/*
 * main.c
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>
#include <sys/fcntl.h>

#include "list.h"
#include "pcm_helpers.h"
#include "square.h"
#include "unimag.h"

int main(int argc, char **argv) {
	short *samples;
	int file_descriptor;
	struct stat file_stats;
	int offset = 0;

	if (argc == 1) {
		printf("%s <audio file>\n", argv[0]);
		return EXIT_FAILURE;
	}

	file_descriptor = open(argv[1], O_RDONLY);
	if (file_descriptor == -1) {
		perror("Failed to open audio file");
		return EXIT_FAILURE;
	}

	if (fstat(file_descriptor, &file_stats)) {
		perror("Failed to stat file");
		return EXIT_FAILURE;
	}

	if (!strncmp((argv[1] + strlen(argv[1] - 4)), ".wav", 4)) {
		offset = 44;
	}

	samples = mmap(0, file_stats.st_size, PROT_READ, MAP_SHARED,
			file_descriptor, offset);

	if (samples == MAP_FAILED) {
		close(file_descriptor);
		perror("Failed to map file");
		return EXIT_FAILURE;
	}

	printf("File length: %d\n", (int) (file_stats.st_size - offset));

	PcmData *pcm = pcm_init(samples, (file_stats.st_size - offset) / 2, false);

	printf("Decoding swipe using Square algorithm...\n");
	SquareDecoder *decoder = square_init(pcm);
	square_decode_swipe(decoder);

	if (decoder->lastError == SwipeError_Success) {
		printf("Decoded card characters: %s\n", decoder->cardString);
	} else {
		printf(
				"Card decoding using Square algorithm has failed! Using UniMag algorithm...\n");
		char *byteSamples = (char*) malloc(
				(file_stats.st_size - offset) * sizeof(char));
		char *buffer = (char*) samples;
		int i = 0;
		for (i = offset; i <= file_stats.st_size; ++i) {
			byteSamples[i] = buffer[i];
		}

		char *cardString = unimag_decode(byteSamples,
				file_stats.st_size - offset);
		printf("Decoded card characters: %s\n", cardString);
		free(byteSamples);
		free(cardString);
	}

	pcm_free(pcm);
	square_free(decoder);

	if (munmap(samples, file_stats.st_size) == -1) {
		perror("Failed to unmap file");
	}
	close(file_descriptor);

	return EXIT_SUCCESS;
}

