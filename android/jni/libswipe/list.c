/*
 * list.c
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#include "list.h"
#include "errors.h"

List* list_init() {
	List *list = (List*) malloc(sizeof(List));

	if (list == NULL) {
		return NULL;
	}

	list->current = NULL;
	list->current_index = -1;
	list->first = NULL;
	list->last = NULL;
	list->size = 0;

	list->lastError = ListError_Success;
	return list;
}

void list_reinit(List *list) {
	ListNode *trav, *prev;

	for (trav = list->first, prev = NULL; trav != NULL;) {
		prev = trav;
		trav = trav->next;
		if (prev) {
			free(prev);
			prev = NULL;
		}
	}
	if (prev) {
		free(prev);
	}

	list->current = NULL;
	list->current_index = -1;
	list->first = NULL;
	list->last = NULL;
	list->size = 0;
	list->lastError = ListError_Success;
}

void list_add(List* list, int value) {
	ListNode *node = (ListNode*) malloc(sizeof(ListNode));

	if (node == NULL) {
		list->lastError = ListError_Malloc;
		return;
	}

	list->current_index++;
	list->current = node;
	node->index = list->current_index;
	node->value = value;
	node->next = NULL;

	if (list->first == NULL) {
		list->first = node;
	}

	if (list->last != NULL) {
		list->last->next = node;
	}

	list->last = node;
	list->size++;

	list->lastError = ListError_Success;
}

/* Gets element at the specified index and leaves pointer at this index */
int list_get(List* list, int index) {
	if (list->current == NULL) {
		list->lastError = ListError_CurrentNull;
		return -1;
	}

	if (index == list->current->index) {
		list->lastError = ListError_Success;
		return list->current->value;
	} else if (index == list->current->index + 1) {
		list->current = list->current->next;
		list->lastError = ListError_Success;
		return list->current->value;
	}

	list->current = list->first;
	while (list->current != NULL) {
		if (list->current->index == index) {
			list->lastError = ListError_Success;
			return list->current->value;
		}
		list->current = list->current->next;
	}

	list->lastError = ListError_InvalidIndex;

	return -1;
}

List* list_free(List* list) {
	if (list == NULL) {
		return NULL;
	}

	list_reinit(list);
	free(list);

	return NULL;
}

