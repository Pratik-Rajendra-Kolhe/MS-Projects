/*
 * Copyright (c) 2001, 2002, 2009
 *	The President and Fellows of Harvard College.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

/*
 * Driver code for whale mating problem
 */
#include <types.h>
#include <lib.h>
#include <thread.h>
#include <test.h>
#include <synch.h>

/*
 * 08 Feb 2012 : GWA : Driver code is in kern/synchprobs/driver.c. We will
 * replace that file. This file is yours to modify as you see fit.
 *
 * You should implement your solution to the whalemating problem below.
 */

// 13 Feb 2012 : GWA : Adding at the suggestion of Isaac Elbaz. These
// functions will allow you to do local initialization. They are called at
// the top of the corresponding driver code.

struct cv *malecv;
struct cv *femalecv;
struct cv *mmcv;
struct lock *lk;
int noOfMales = 0;
int noOfFemales = 0;
int noOfMatchmakers = 0;

void whalemating_init() {
	malecv = cv_create("malecv");
	femalecv = cv_create("femalecv");
	mmcv = cv_create("mmcv");
    lk = lock_create("mainlock");
}

// 20 Feb 2012 : GWA : Adding at the suggestion of Nikhil Londhe. We don't
// care if your problems leak memory, but if you do, use this to clean up.

void whalemating_cleanup() {
	cv_destroy(malecv);
	cv_destroy(femalecv);
	cv_destroy(mmcv);
	lock_destroy(lk);
}

void
male(void *p, unsigned long which)
{
	struct semaphore *whalematingMenuSemaphore = (struct semaphore *)p;
	(void)which;
	// Implement this function

	male_start();

	lock_acquire(lk);
	noOfMales++;
	if (noOfFemales && noOfMatchmakers) {
		cv_signal(femalecv, NULL);
		cv_signal(mmcv, NULL);
	} else {
		cv_wait(malecv, lk);
	}
	male_end();
	noOfMales--;
	lock_release(lk);

	// 08 Feb 2012 : GWA : Please do not change this code. This is so that your
	// whalemating driver can return to the menu cleanly.
	V(whalematingMenuSemaphore);
	return;
}

void
female(void *p, unsigned long which)
{
	struct semaphore * whalematingMenuSemaphore = (struct semaphore *)p;
	(void)which;
	// Implement this function

	female_start();

	lock_acquire(lk);
	noOfFemales++;
	if (noOfMales && noOfMatchmakers) {
		cv_signal(malecv, NULL);
		cv_signal(mmcv, NULL);
	} else {
		cv_wait(femalecv, lk);
	}
	female_end();
	noOfFemales--;
	lock_release(lk);

	// 08 Feb 2012 : GWA : Please do not change this code. This is so that your
	// whalemating driver can return to the menu cleanly.
	V(whalematingMenuSemaphore);
	return;
}

void
matchmaker(void *p, unsigned long which)
{
	struct semaphore * whalematingMenuSemaphore = (struct semaphore *)p;
	(void)which;
	// Implement this function

	matchmaker_start();

	lock_acquire(lk);
	noOfMatchmakers++;
	if (noOfMales && noOfFemales) {
		cv_signal(femalecv, NULL);
		cv_signal(malecv, NULL);
	} else {
		cv_wait(mmcv, lk);
	}
	matchmaker_end();
	noOfMatchmakers--;
	lock_release(lk);

	// 08 Feb 2012 : GWA : Please do not change this code. This is so that your
	// whalemating driver can return to the menu cleanly.
	V(whalematingMenuSemaphore);
	return;
}

/*
 * You should implement your solution to the stoplight problem below. The
 * quadrant and direction mappings for reference: (although the problem is,
 * of course, stable under rotation)
 *
 *   | 0 |
 * --     --
 *    0 1
 * 3       1
 *    3 2
 * --     --
 *   | 2 | 
 *
 * As way to think about it, assuming cars drive on the right: a car entering
 * the intersection from direction X will enter intersection quadrant X
 * first.
 *
 * You will probably want to write some helper functions to assist
 * with the mappings. Modular arithmetic can help, e.g. a car passing
 * straight through the intersection entering from direction X will leave to
 * direction (X + 2) % 4 and pass through quadrants X and (X + 3) % 4.
 * Boo-yah.
 *
 * Your solutions below should call the inQuadrant() and leaveIntersection()
 * functions in drivers.c.
 */

// 13 Feb 2012 : GWA : Adding at the suggestion of Isaac Elbaz. These
// functions will allow you to do local initialization. They are called at
// the top of the corresponding driver code.

struct lock *quadlock[4];
struct semaphore *allow3sem;

void stoplight_init() {
	quadlock[0] = lock_create("quad0lock");
	quadlock[1] = lock_create("quad1lock");
	quadlock[2] = lock_create("quad2lock");
	quadlock[3] = lock_create("quad3lock");
	allow3sem = sem_create("allow3sem", 3);
	return;
}

// 20 Feb 2012 : GWA : Adding at the suggestion of Nikhil Londhe. We don't
// care if your problems leak memory, but if you do, use this to clean up.

void stoplight_cleanup() {
	lock_destroy(quadlock[0]);
	lock_destroy(quadlock[1]);
	lock_destroy(quadlock[2]);
	lock_destroy(quadlock[3]);
	sem_destroy(allow3sem);
	return;
}

void
gostraight(void *p, unsigned long direction)
{
	struct semaphore * stoplightMenuSemaphore = (struct semaphore *)p;
	(void)direction;

	P(allow3sem);

	lock_acquire(quadlock[direction]);
	inQuadrant(direction);

	lock_acquire(quadlock[(direction + 3) % 4]);
	inQuadrant((direction + 3) % 4);
	lock_release(quadlock[direction]);

	leaveIntersection();
	lock_release(quadlock[(direction + 3) % 4]);

	V(allow3sem);

	// 08 Feb 2012 : GWA : Please do not change this code. This is so that your
	// stoplight driver can return to the menu cleanly.
	V(stoplightMenuSemaphore);
	return;
}

void
turnleft(void *p, unsigned long direction)
{
	struct semaphore * stoplightMenuSemaphore = (struct semaphore *)p;
	(void)direction;

	P(allow3sem);

	lock_acquire(quadlock[direction]);
	inQuadrant(direction);

	lock_acquire(quadlock[(direction + 3) % 4]);
	inQuadrant((direction + 3) % 4);
	lock_release(quadlock[direction]);

	lock_acquire(quadlock[(direction + 2) % 4]);
	inQuadrant((direction + 2) % 4);
	lock_release(quadlock[(direction + 3) % 4]);

	leaveIntersection();
	lock_release(quadlock[(direction + 2) % 4]);

	V(allow3sem);

	// 08 Feb 2012 : GWA : Please do not change this code. This is so that your
	// stoplight driver can return to the menu cleanly.
	V(stoplightMenuSemaphore);
	return;
}

void
turnright(void *p, unsigned long direction)
{
	struct semaphore * stoplightMenuSemaphore = (struct semaphore *)p;
	(void)direction;

	P(allow3sem);
	lock_acquire(quadlock[direction]);
	inQuadrant(direction);
	leaveIntersection();
	lock_release(quadlock[direction]);
	V(allow3sem);

	// 08 Feb 2012 : GWA : Please do not change this code. This is so that your
	// stoplight driver can return to the menu cleanly.
	V(stoplightMenuSemaphore);
	return;
}
