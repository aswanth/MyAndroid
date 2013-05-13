/*
 * list.h
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#ifndef LIST_H_
#define LIST_H_

#include <stdlib.h>

typedef struct list_node {
	int value;
	int index;
	struct list_node *next;
} ListNode;

typedef struct linked_list {
	int size;
	int current_index;
	int lastError;
	struct list_node *first;
	struct list_node *current;
	struct list_node *last;
} List;

List* list_init();
void list_reinit(List* list);
void list_add(List* list, int value);
int list_get(List* list, int index);
List* list_free(List* list);

#endif /* LIST_H_ */
