/*
 * unimag.h
 *
 *  Created on: 13-03-2012
 *      Author: Kamil Krzywicki, Marek Krasnowski
 */

#ifndef UNIMAG_H_
#define UNIMAG_H_

/* Card string is NULL-terminated. Free the char* pointer after using it. */
char* unimag_decode(signed char *samples, long length);
int unimag_add_wave(int value);

#endif /* UNIMAG_H_ */
