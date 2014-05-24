/*
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2008, 2009
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

#include <types.h>
#include <kern/errno.h>
#include <lib.h>
#include <spl.h>
#include <spinlock.h>
#include <thread.h>
#include <current.h>
#include <mips/tlb.h>
#include <addrspace.h>
#include <vm.h>
#include <coremap.h>

/*
 * Non-Dumb MIPS-only "VM system" that is intended to only be just barely
 * enough to struggle off the ground. You should replace all of this
 * code while doing the VM assignment. In fact, starting in that
 * assignment, this file is not included in your kernel!
 */

void
vm_tlbshootdown_all(void)
{
	/* Disable interrupts on this CPU while frobbing the TLB. */
	int spl = splhigh();

	for (int i=0; i<NUM_TLB; i++) {
		tlb_write(TLBHI_INVALID(i), TLBLO_INVALID(), i);
	}

	splx(spl);

}

void
vm_tlbshootdown(const struct tlbshootdown *ts)
{
	(void)ts;
	panic("non-dumbvm tried to do tlb shootdown?!\n");
}

static
void
as_zero_region(paddr_t paddr, unsigned npages)
{
	bzero((void *)PADDR_TO_KVADDR(paddr), npages * PAGE_SIZE);
}

int
vm_fault(int faulttype, vaddr_t faultaddress)
{
	faultaddress &= PAGE_FRAME;

	struct addrspace *as = curthread->t_addrspace;
	paddr_t paddr;
	struct page_table *temp = &as->start;
	struct page_entry *match = NULL;

	if(faultaddress >= USERSTACK || faultaddress < as->vstart) {
		return EFAULT;
	}

	while(temp != NULL) {
		for(int i=0;i<PTABLE_ARRAY_SIZE;i++) {
			if(temp->table[i].page_paddr != 0 && temp->table[i].page_vaddr == faultaddress) {
				match = &temp->table[i];
				break;
			} else if(temp->table[i].page_paddr == 0) {
				break;
			}
		}
		if(match != NULL) {
			break;
		}
		temp = temp->nextPageTable;
	}

	switch (faulttype) {
	case VM_FAULT_READONLY:
		return EFAULT;
		break;
	case VM_FAULT_READ:
		if(match != NULL) {
			paddr = match->page_paddr;
		} else {
			return EFAULT;
		}
		break;
	case VM_FAULT_WRITE:
		if(match != NULL) {
			if(match->isWrite == 0) {
//				spinlock_acquire(&coremap_slock);
//				paddr = getpaddr(1);
//				if(paddr == 0) {
//					spinlock_release(&coremap_slock);
//					return ENOMEM;
//				}
//				addtocoremap(paddr, 1, 0);
//				spinlock_release(&coremap_slock);
//				match->isWrite = 1;
//				match->isShared = 0;
//				memcpy((void*)PADDR_TO_KVADDR(paddr), (void*)PADDR_TO_KVADDR(match->page_paddr), PAGE_SIZE);
//				match->page_paddr = paddr;
				return EFAULT;
			} else {
				paddr = match->page_paddr;
			}
		} else if(faultaddress >= STACK_BASE && faultaddress < USERSTACK) {
			spinlock_acquire(&coremap_slock);
			paddr = getpaddr(1);
			if(paddr == 0) {
				spinlock_release(&coremap_slock);
				return ENOMEM;
			}
			addtocoremap(paddr, 1, 0);
			spinlock_release(&coremap_slock);
			addPageEntry(faultaddress, paddr, 1, 1, 0);
			as_zero_region(paddr, 1);
		} else {
			return EFAULT;
		}
		break;
	default:
		return EINVAL;
	}

	/* Disable interrupts on this CPU while frobbing the TLB. */
	int spl = splhigh();
	uint32_t ehi, elo;

	for (int i=0; i<NUM_TLB; i++) {
		tlb_read(&ehi, &elo, i);
		if (elo & TLBLO_VALID) {
			continue;
		}
		ehi = faultaddress;
//		if(match->isWrite) {
			elo = paddr | TLBLO_DIRTY | TLBLO_VALID;
//		} else {
//			elo = (paddr & (~TLBLO_DIRTY)) | TLBLO_VALID;
//		}
		tlb_write(ehi, elo, i);
		splx(spl);
		return 0;
	}
	ehi = faultaddress;
//	if(match->isWrite) {
		elo = paddr | TLBLO_DIRTY | TLBLO_VALID;
//	} else {
//		elo = (paddr & (~TLBLO_DIRTY)) | TLBLO_VALID;
//	}
	tlb_random(ehi, elo);

	splx(spl);

	return 0;
}

struct addrspace *
as_create(void)
{
	struct addrspace *as = kmalloc(sizeof(struct addrspace));
	if (as==NULL) {
		return NULL;
	}
	as->vbreakbase = 0;
	as->vbreaktop = 0;
	as->heapNPages = 0;
	as->vstart = 0;
	as->start.entry_count = 0;
	as->start.nextPageTable = NULL;
	as->codeNPages = 0;
	as->dataNPages = 0;
	as->end = &as->start;
	int i;
	for(i=0;i<PTABLE_ARRAY_SIZE;i++) {
		as->start.table[i].page_paddr = 0;
//		as->start.table[i].isShared = 0;
	}
	return as;
}

void
as_destroy(struct addrspace *as)
{
	struct page_table *temp = &as->start;
	while(temp != NULL) {
		for(int i=0;i<PTABLE_ARRAY_SIZE;i++) {
			if(temp->table[i].page_paddr != 0) {
				page_free(temp->table[i].page_paddr);
			} else if(temp->table[i].page_paddr == 0) {
				break;
			}
		}
		temp = temp->nextPageTable;
	}
	temp = as->start.nextPageTable;
	struct page_table *temp1;
	while(temp != NULL) {
		temp1 = temp->nextPageTable;
		kfree(temp);
		temp = temp1;
	}
	kfree(as);
}

void
as_activate(struct addrspace *as)
{
	int i, spl;

	(void)as;

	/* Disable interrupts on this CPU while frobbing the TLB. */
	spl = splhigh();

	for (i=0; i<NUM_TLB; i++) {
		tlb_write(TLBHI_INVALID(i), TLBLO_INVALID(), i);
	}
	tlb_broadcast();

	splx(spl);
}

int
as_define_region(struct addrspace *as, vaddr_t vaddr, size_t sz,
		 int readable, int writeable, int executable)
{
	size_t npages;

	/* Align the region. First, the base... */
	sz += vaddr & ~(vaddr_t)PAGE_FRAME;
	vaddr &= PAGE_FRAME;

	/* ...and now the length. */
	sz = (sz + PAGE_SIZE - 1) & PAGE_FRAME;

	npages = sz / PAGE_SIZE;

	(void)readable;
	(void)writeable;
	(void)executable;

	if(as->codeNPages == 0) {
		as->vstart = vaddr;
		as->codeNPages = npages;
		as->vbreakbase = npages * PAGE_SIZE + vaddr;
		as->vbreaktop = as->vbreakbase;
		return 0;
	}

	if(as->dataNPages == 0) {
		as->dataNPages = npages;
		as->vbreakbase = npages * PAGE_SIZE + vaddr;
		as->vbreaktop = as->vbreakbase;
		return 0;
	}

	/*
	 * Support for more than two regions is not available.
	 */
	kprintf("myvm: Warning: too many regions\n");
	return EUNIMP;

}

int
as_prepare_load(struct addrspace *as)
{
	paddr_t paddr;
	for(unsigned int i=0;i<as->codeNPages;i++) {
		spinlock_acquire(&coremap_slock);
		paddr = getpaddr(1);
		if(paddr == 0) {
			spinlock_release(&coremap_slock);
			as_destroy(as);
			return ENOMEM;
		}
		addtocoremap(paddr, 1, 0);
		spinlock_release(&coremap_slock);
		addPageEntry(as->vstart + (PAGE_SIZE * i), paddr, 1, 1, 0);
		as_zero_region(paddr, 1);
	}
	vaddr_t vdataStart = as->vbreakbase - (as->dataNPages * PAGE_SIZE);
	for(unsigned int i=0;i<as->dataNPages;i++) {
		spinlock_acquire(&coremap_slock);
		paddr = getpaddr(1);
		if(paddr == 0) {
			spinlock_release(&coremap_slock);
			as_destroy(as);
			return ENOMEM;
		}
		addtocoremap(paddr, 1, 0);
		spinlock_release(&coremap_slock);
		addPageEntry(vdataStart + (PAGE_SIZE * i), paddr, 1, 1, 0);
		as_zero_region(paddr, 1);
	}
	return 0;
}

int
as_complete_load(struct addrspace *as)
{
	struct page_table *temp = &as->start;
	vaddr_t codeTop = as->vstart + (PAGE_SIZE * as->codeNPages);
	vaddr_t dataTop = as->vbreakbase;
	while(temp != NULL) {
		for(int i=0;i<PTABLE_ARRAY_SIZE;i++) {
			if(temp->table[i].page_paddr == 0) {
				break;
			}
			if(temp->table[i].page_vaddr < codeTop) {
				temp->table[i].isExe = 1;
				temp->table[i].isRead = 1;
				temp->table[i].isWrite = 0;
			} else if(temp->table[i].page_vaddr < dataTop) {
				temp->table[i].isExe = 0;
				temp->table[i].isRead = 1;
				temp->table[i].isWrite = 1;
			}
		}
		temp = temp->nextPageTable;
	}
	return 0;
}

int
as_define_stack(struct addrspace *as, vaddr_t *stackptr)
{
	(void)as;
	*stackptr = USERSTACK;
	return 0;
}

int
as_copy(struct addrspace *old, struct addrspace **ret)
{
	struct addrspace *new;

	new = as_create();
	if (new==NULL) {
		return ENOMEM;
	}
	struct page_table *oldtemp = &old->start;
	struct page_table *newtemp = &new->start;
	paddr_t paddr;
	while(oldtemp != NULL) {
		if(oldtemp != &old->start) {
			struct page_table *temp = (struct page_table*)kmalloc(sizeof(struct page_table));
			temp->nextPageTable = NULL;
			newtemp->nextPageTable = temp;
			newtemp = temp;
		}
		newtemp->entry_count = oldtemp->entry_count;
		for(int i=0;i<PTABLE_ARRAY_SIZE;i++) {
			newtemp->table[i].page_vaddr = oldtemp->table[i].page_vaddr;
			newtemp->table[i].isExe = oldtemp->table[i].isExe;
			newtemp->table[i].isRead = oldtemp->table[i].isRead;
			newtemp->table[i].isWrite = oldtemp->table[i].isWrite;
//			newtemp->table[i].isShared = 1;
//			newtemp->table[i].page_paddr = oldtemp->table[i].page_paddr;
			if(oldtemp->table[i].page_paddr != 0) {
				spinlock_acquire(&coremap_slock);
				paddr = getpaddr(1);
				if(paddr == 0) {
					spinlock_release(&coremap_slock);
					as_destroy(new);
					return ENOMEM;
				}
				addtocoremap(paddr,1,0);
				newtemp->table[i].page_paddr = paddr;
				spinlock_release(&coremap_slock);
				memcpy((void *)PADDR_TO_KVADDR(newtemp->table[i].page_paddr), (void *)PADDR_TO_KVADDR(oldtemp->table[i].page_paddr), PAGE_SIZE);
			} else {
				newtemp->table[i].page_paddr = 0;
			}
		}
		oldtemp = oldtemp->nextPageTable;
	}

	*ret = new;
	return 0;
}

void addPageEntry(vaddr_t vaddr, paddr_t paddr, int isRead, int isWrite, int isExe) {
	struct page_table *temp = &curthread->t_addrspace->start;
	while(temp != NULL) {
		int i;
		if(temp->entry_count != PTABLE_ARRAY_SIZE) {
			for(i=0;i<PTABLE_ARRAY_SIZE;i++) {
				if(temp->table[i].page_paddr == 0) {
					temp->table[i].isExe = isExe;
					temp->table[i].isRead = isRead;
					temp->table[i].isWrite = isWrite;
					temp->table[i].page_paddr = paddr;
					temp->table[i].page_vaddr = vaddr;
					temp->entry_count++;
					return;
				}
			}
		}
		temp = temp->nextPageTable;
	}
	temp = (struct page_table*)kmalloc(sizeof(struct page_table));
	temp->nextPageTable = NULL;
	temp->table[0].isExe = isExe;
	temp->table[0].isRead = isRead;
	temp->table[0].isWrite = isWrite;
	temp->table[0].page_paddr = paddr;
	temp->table[0].page_vaddr = vaddr;
	temp->entry_count = 1;
	int i;
	for(i=1;i<PTABLE_ARRAY_SIZE;i++) {
		temp->table[i].page_paddr = 0;
	}
	curthread->t_addrspace->end->nextPageTable = temp;
	curthread->t_addrspace->end = temp;
}

paddr_t removePageEntry(vaddr_t vaddr) {
	struct page_table *temp = &curthread->t_addrspace->start;
	paddr_t paddr;
	while(temp != NULL) {
		int i;
		for(i=0;i<PTABLE_ARRAY_SIZE;i++) {
			if(temp->table[i].page_vaddr == vaddr) {
				paddr = temp->table[i].page_paddr;
				temp->table[i].page_paddr = 0;
				temp->entry_count--;
				return paddr;
			}
		}
		temp = temp->nextPageTable;
	}
	return 0;
}
