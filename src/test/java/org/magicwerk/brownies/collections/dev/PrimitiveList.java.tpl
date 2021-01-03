	public static I{NAME}List of({PRIMITIVE}[] values) {
		return new Immutable{NAME}ListArrayPrimitive(values);
	}

	public static I{NAME}List of({WRAPPER}[] values) {
		return new Immutable{NAME}ListArrayWrapper(values);
	}

	public static I{NAME}List of(List<{WRAPPER}> values) {
		return new Immutable{NAME}ListList(values);
	}

    static class Immutable{NAME}ListArrayPrimitive extends Immutable{NAME}List {
    	{PRIMITIVE}[] values;

    	public Immutable{NAME}ListArrayPrimitive({PRIMITIVE}[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected {PRIMITIVE} doGet(int index) {
			return values[index];
		}
    }

    static class Immutable{NAME}ListArrayWrapper extends Immutable{NAME}List {
    	{WRAPPER}[] values;

    	public Immutable{NAME}ListArrayWrapper({WRAPPER}[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected {PRIMITIVE} doGet(int index) {
			return values[index];
		}
    }

    static class Immutable{NAME}ListList extends Immutable{NAME}List {
    	List<{WRAPPER}> values;

    	public Immutable{NAME}ListList(List<{WRAPPER}> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected {PRIMITIVE} doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class Immutable{NAME}List extends I{NAME}List {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, {PRIMITIVE} key) {
			return {NAME}BinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public I{NAME}List unmodifiableList() {
			return this;
		}

		@Override
		protected {PRIMITIVE} getDefaultElem() {
			return {DEFAULT};
		}

        /**
         * Throw exception if an attempt is made to change an immutable list.
         */
        private void error() {
            throw new UnsupportedOperationException("list is immutable");
        }

        //-- Writers

        @Override
        protected void doRemoveAll(int index, int len) {
        	error();
        }

        @Override
        protected void doClear() {
        	error();
        }

        @Override
        protected void doModify() {
        	error();
        }

		@Override
		protected void doClone(I{NAME}List that) {
			error();
		}

		@Override
		protected {PRIMITIVE} doSet(int index, {PRIMITIVE} elem) {
			error();
			return {DEFAULT};
		}

		@Override
		protected {PRIMITIVE} doReSet(int index, {PRIMITIVE} elem) {
			error();
			return {DEFAULT};
		}

		@Override
		protected boolean doAdd(int index, {PRIMITIVE} elem) {
			error();
			return false;
		}

		@Override
		protected void doEnsureCapacity(int minCapacity) {
			error();
		}

		@Override
		public void trimToSize() {
			error();
		}

		@Override
		protected I{NAME}List doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(I{NAME}List that) {
			error();
		}

		@Override
		protected {PRIMITIVE} doRemove(int index) {
			error();
			return {DEFAULT};
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }
