//http://www.flowerfire.com/cs497rej/et++/src/CONTAINER/OrdColl.h

#ifndef OrdColl_First
#define OrdColl_First
#ifdef __GNUG__
#pragma interface
#endif

#include "SeqColl.h"

class OrdCollectionIter;
class RevOrdCollectionIter;

//----- OrdCollection ----------------------------------------------------------

class OrdCollection : public SeqCollection {
friend OrdCollectionIter;
friend RevOrdCollectionIter;
public:
	MetaDef(OrdCollection);
	OrdCollection(int aCapacity= 1);
	~OrdCollection();
	void InitNew();
	void Empty(int newCapacity= cContainerInitCap);
	void FreeAll();

	//---- accessing
	Iterator *MakeIterator(bool forward= cIterForward, void *placement= 0);

	//---- sorting/searching
	void Sort();                    // based on Equal
	int BinarySearch(Object *);     // receiver must be sorted! -1 == not found

	//---- instrumentation
	virtual void CheckInvariant();

public:
	static const cDefaultCapacity;
	static const cMinExpand;
	static const cShrinkFactor;

protected:
	//---- primitives
	Object *primAddAt(Object *op, int idx);
	Object *primRemoveAt(int idx);
	Object *primPutAt(Object *op, int idx);
	Object *primAt(int idx);
	Object *primIndexOf(Object *op, int offset, int *idx);
	Object *primIndexOfPtr(Object *op, int offset, int *idx);

	//---- implementation
	int PhysIndex(int idx);
	int LogIndex(int phx);
	void MoveGapTo(int newGapstart);
	void Init(int aCapacity);
	bool LowWaterMark();
	void SetCapacity(int newCapacity);

protected:
	Object **cont;
	int capacity;
	int gapStart;
	int gapSize;
};

//---- OrdCollectionIter -------------------------------------------------------

class OrdCollectionIter : public Iterator {
friend OrdCollection;
public:
	OrdCollectionIter(OrdCollection *s);
	~OrdCollectionIter();

	Object *operator()();
protected:
	Container *GetContainer();
	void Detach();

	//---- robust iterators
	virtual void OnEmpty();
	virtual void OnAddAt(int idx);
	virtual void OnRemoveAt(int idx);

protected:
	OrdCollection *seq;
	int ce;
};

//---- RevOrdCollectionIter ----------------------------------------------------

class RevOrdCollectionIter : public OrdCollectionIter {
public:
	RevOrdCollectionIter(OrdCollection *s);

	Object *operator()();
protected:
	//---- robust iterators
	void OnEmpty();
	void OnAddAt(int idx);
	void OnRemoveAt(int idx);
};

//---- OrdCollection inlines ---------------------------------------------------

inline bool OrdCollection::LowWaterMark()
	{  return (bool) (size < (capacity / 4) && size > cContainerInitCap); }

inline int OrdCollection::PhysIndex(int idx)
	{ return (idx < gapStart) ? idx : idx + gapSize; }

inline int OrdCollection::LogIndex(int phx)
	{ return (phx < gapStart) ? phx : phx - gapSize; }

#endif


#include "Col.ph"
#ifdef __GNUG__
#pragma implementation
#endif

#include "OrdColl.h"
#include "Class.h"                        
#include "IterIter.h"
#include "ContainerTypes.h"
#include "Math.h"
#include "Invariant.h"
#include "Error.h"
#include "System.h"
#include "ET_stdlib.h"

//---- OrdCollection -----------------------------------------------------------

NewMetaImpl(OrdCollection,SeqCollection, (TVP(cont,capacity), T(gapStart), T(gapSize)));

InvariantChecker(OrdCollection);

const int OrdCollection::cDefaultCapacity= 1;
const int OrdCollection::cMinExpand= 8;
const int OrdCollection::cShrinkFactor= 2;

OrdCollection::OrdCollection(int aCapacity)
{
	if (aCapacity < 0) {
		Warning("OrdCollection", "aCapacity (%d) < 0", aCapacity);
		aCapacity= cDefaultCapacity;
	} else if (aCapacity == 0)
		aCapacity= cDefaultCapacity;
	Init(aCapacity);
}

OrdCollection::~OrdCollection()
{
	SafeDelete(cont);
}

void OrdCollection::InitNew()
{
	SeqCollection::InitNew();
	Init(cDefaultCapacity);
}

void OrdCollection::Empty(int newCapacity)
{
	if (newCapacity < 0) {
		Warning("Empty", "newCapacity (%d) < 0", newCapacity);
		newCapacity= cDefaultCapacity;
	} else if (newCapacity == 0)
		newCapacity= cDefaultCapacity;

	SafeDelete(cont);
	Init(newCapacity);
	size= 0;

	if (HasIterators())
		ForEachIterDo(OrdCollectionIter,OnEmpty());
}

void OrdCollection::FreeAll()
{
	for (int i= 0; i < Size(); i++ ) {
		Object *op= At(i);
		op->FreeAll();
		SafeDelete(op);
	}
	Empty(cDefaultCapacity);
}
						
//---- accessing ----

Iterator *OrdCollection::MakeIterator(bool forward, void *placement)
{
	if (forward)
		return new(placement) OrdCollectionIter(this);
	return new(placement) RevOrdCollectionIter(this);
}

//---- sorting/searching ----

void OrdCollection::Sort()
{
	AssertInvariant(OrdCollection);

	MoveGapTo(capacity - gapSize);
	::qsort(cont, size, sizeof(Object*), &::ppCompare);
	Changed();
}

int OrdCollection::BinarySearch(Object *op)
{
	register int result;

	AssertInvariant(OrdCollection);

	if (op == 0)
		return -1;

	MoveGapTo(capacity - gapSize);

	register int base= 0;
	register int last= base + Size() - 1;
	while (last >= base) {
		register position= (base + last) / 2;
		register Object *op2= cont[position];
		if ((op2 == 0) || (result= op->Compare(op2)) == 0)
			return LogIndex(position);
		if (result < 0)
			last= position - 1;
		else
			base= position + 1;
	}
	return -1;
}

//---- instrumentation ----

void OrdCollection::CheckInvariant()
{
	int i;

	Assert(size >= 0);
	Assert(capacity > 0);
	Assert(gapSize >= 0);
	Assert(gapStart >= 0);
	Assert(gapStart <= capacity);
	Assert(size + gapSize == capacity);

	if (gSystem && GetAssertLevel() >= 10) {
		for (i= 0; i < gapStart; i++)
			Assert(cont[i]);
		for (i= gapStart + gapSize; i < capacity; i++)
			Assert(cont[i]);
		for (i= gapStart; i < gapStart + gapSize; i++)
			Assert(! cont[i]);
	}
}

//---- primitives ----

Object *OrdCollection::primAt(int idx)
{
	return cont[PhysIndex(idx)];
}

Object *OrdCollection::primAddAt(Object *op, int idx)
{
	int physIdx;

	AssertInvariant(OrdCollection);
		
	if (gapSize <= 0)
		SetCapacity(GrowBy(Math::Max(capacity, cMinExpand)));

	if (idx == gapStart) {
		physIdx= gapStart;
		gapStart++;
	} else {
		physIdx= PhysIndex(idx);
		if (physIdx < gapStart) {
			MoveGapTo(physIdx);
			physIdx= gapStart;
			gapStart++;
		} else {
			MoveGapTo(physIdx - gapSize);
			physIdx= gapStart + gapSize - 1;
		}
	}
	Assert(physIdx >= 0 && physIdx < capacity);
	cont[physIdx]= op;
	gapSize--;
	size++;

	if (HasIterators())
		ForEachIterDo(OrdCollectionIter,OnAddAt(idx));
	Changed();
	return 0;
}

Object *OrdCollection::primRemoveAt(int idx)
{
	register int physIdx;
	Object *removed;

	AssertInvariant(OrdCollection);

	if (idx == gapStart - 1 || idx == gapStart) {
		if ( idx == gapStart )
			physIdx= gapStart + gapSize;        // at right boundary
		else
			physIdx= --gapStart;                // at left boundary
	} else {
		physIdx= PhysIndex(idx);
		if ( physIdx < gapStart ) {
			MoveGapTo(physIdx + 1);
			physIdx= --gapStart;
		} else {
			MoveGapTo(physIdx - gapSize);
			physIdx= gapStart + gapSize;
		}
	}
	Assert(physIdx >= 0 && physIdx < capacity);
	removed= cont[physIdx];
	cont[physIdx]= 0;
	gapSize++;
	size--;
		
	if (LowWaterMark()) {
		int newCapacity= Math::Max(capacity / cShrinkFactor, 1);
		if (capacity > newCapacity)
			SetCapacity(newCapacity);
	}

	if (HasIterators())
		ForEachIterDo(OrdCollectionIter,OnRemoveAt(idx));
	Changed();
	return removed;
}

Object *OrdCollection::primPutAt(Object *op, int idx)
{
	AssertInvariant(OrdCollection);
		
	int phx= PhysIndex(idx);
	Assert(phx >= 0 && phx < capacity);
	Object *removed= cont[phx];
	cont[phx]= op;
	Changed();
	return removed;
}

Object *OrdCollection::primIndexOf(Object *op, int offset, int *idx)
{
	register int i;
				
	AssertInvariant(OrdCollection);

	for (i= 0; i < Size(); i++) {
		if (cont[PhysIndex(i)]->IsEqual(op)) {
			int edx= i + offset;
			if (edx >= 0 && edx < Size()) {
				if (idx)
					*idx= edx;
				return cont[PhysIndex(edx)];
			}
			break;
		}
	}
	if (idx)
		*idx= -1;
	return 0;
}

Object *OrdCollection::primIndexOfPtr(Object *op, int offset, int *idx)
{
	register int i;
				
	AssertInvariant(OrdCollection);

	for (i= 0; i < Size(); i++) {
		if (cont[PhysIndex(i)] == op) {
			int edx= i + offset;
			if (edx >= 0 && edx < Size()) {
				if (idx)
					*idx= edx;
				return cont[PhysIndex(edx)];
			}
			break;
		}
	}
	if (idx)
		*idx= -1;
	return 0;
}

//---- implementation ----

void OrdCollection::MoveGapTo(int start)
{
	register int i;
		
	AssertInvariant(OrdCollection);
	Assert(start + gapSize - 1 < capacity);

	if (gapSize <= 0) {
		gapStart= start;
		return;
	}
	if (start < gapStart) {
		for (i= gapStart - 1; i >= start; i--)
			cont[i + gapSize]= cont[i];
	} else if (start > gapStart) {
		register int stop= start + gapSize;
		for (i= gapStart + gapSize; i < stop; i++)
			cont[i - gapSize]= cont[i];
	}
	gapStart= start;
	for (i= gapStart; i < gapStart + gapSize; i++)
		cont[i]= 0;     // null slots for Inspector
}

void OrdCollection::Init(int aCapacity)
{
	capacity= aCapacity;
	cont= new Object *[capacity];
	gapStart= 0;
	gapSize= aCapacity;
}

void OrdCollection::SetCapacity(int newCapacity)
{
	AssertInvariant(OrdCollection);
	Assert(newCapacity > 0);
	Assert(size <= newCapacity);
	Assert(capacity != newCapacity);

	int newGapSize= newCapacity - size;
	MoveGapTo(capacity - gapSize);
	cont= (Object **) Storage::ReAlloc(cont, newCapacity * sizeof(Object *));
	if (newCapacity > capacity)
		for (int i= capacity; i < newCapacity; i++)
			cont[i]= 0;
	gapSize= newGapSize;
	capacity= newCapacity;
}

//---- OrdCollectionIter -------------------------------------------------------

OrdCollectionIter::OrdCollectionIter(OrdCollection *s)
{
	seq= s;
	ce= -1;
}

OrdCollectionIter::~OrdCollectionIter()
{
	Terminate();
}

Container *OrdCollectionIter::GetContainer()
{
	return seq;
}

Object *OrdCollectionIter::operator()()
{
	if (! Activated()) {
		Activate();
		ce= -1;
	}
	if (! Terminated() && ++ce < seq->Size())
		return seq->primAt(ce);
	else {
		Terminate();
		return 0;
	}
}

void OrdCollectionIter::Detach()
{
	seq= 0;
	Iterator::Detach();
}

void OrdCollectionIter::OnEmpty()
{
	if (IsActive())
		ce= -1;
}

void OrdCollectionIter::OnAddAt(int idx)
{
	if (IsActive() && idx <= ce)
		ce++;
}

void OrdCollectionIter::OnRemoveAt(int idx)
{
	if (IsActive() && idx <= ce)
		ce--;
}

//---- RevOrdCollectionIter ----------------------------------------------------

RevOrdCollectionIter::RevOrdCollectionIter(OrdCollection *s) : OrdCollectionIter(s)
{
	ce= seq->Size();
}

Object *RevOrdCollectionIter::operator()()
{
	if (! Activated()) {
		Activate();
		ce= seq->Size();
	}
	if (IsActive() && --ce >= 0)
		return seq->At(ce);
	Terminate();
	return 0;
}

void RevOrdCollectionIter::OnEmpty()
{
	if (IsActive())
		ce= 0;
}

void RevOrdCollectionIter::OnRemoveAt(int idx)
{
	if (IsActive() && idx < ce)
		ce--;
}

void RevOrdCollectionIter::OnAddAt(int idx)
{
	if (IsActive() && idx <= ce)
		ce++;
}


