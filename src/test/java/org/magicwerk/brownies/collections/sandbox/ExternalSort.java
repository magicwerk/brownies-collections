package org.magicwerk.brownies.collections.sandbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.StreamTools;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class ExternalSort<E> {
	/** Logger */
	private static Logger LOG = (Logger) LoggerFactory.getLogger(ExternalSort.class);

	/** Number of elements each chunk of objects written to a temporary file will have */
	private int chunkSize = 1000;
	/** Comparator used for sorting */
	private Comparator<? super E> comparator;

	
    public ExternalSort() {
    }

    public ExternalSort(Comparator<? super E> comparator) {
		this.comparator = comparator;
	}
	
    public ExternalSort(Comparator<? super E> comparator, int chunkSize) {
        this.comparator = comparator;
        this.chunkSize = chunkSize;
    }
    
    public int getChunkSize() {
        return chunkSize;
    }

	void setChunkSize(int chunkSize) {
	    this.chunkSize = chunkSize;
	}
	
	public Comparator<? super E> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    public Iterator<E> sort(final Iterator<E> iter) {
		return new Iterator<E>() {		
		    Comparator<? super E> valueComparator = ExternalSort.this.comparator;
		    
			int chunk;
			GapList<File> files;

			E[] values;
			ObjectInputStream[] valueReaders;
			
			boolean nextValueReady;
			boolean hasNextValue;
			E nextValue;
			
			/** true if the values have been sorted */
			boolean sorted = false;

			@Override
			public boolean hasNext() {
			    if (nextValueReady) {
			        return hasNextValue;
			    }
                nextValueReady = true;
				if (!sorted) {
					sort();
					sorted = true;
				}
				getValue();
				return hasNextValue;
			}

			@Override
			public E next() {
			    if (!nextValueReady) {
			        hasNext();
			    }
                nextValueReady = false;
			    if (!hasNextValue) {
			        throw new NoSuchElementException();
			    }
				return nextValue;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		
			/**
			 * Create tempory files containing sorted chunks
			 * and setup readers for iterating.
			 */
			void sort() {
				try {
				    // Create tempory files containing sorted chunks
					files = new GapList<File>();
					final GapList<E> list = new GapList<E>(chunkSize);
					while (true) {
						boolean next = iter.hasNext();
						if (next) {
							E val = iter.next(); 
							list.add(val);
							
							if (valueComparator == null) {
							    valueComparator = (Comparator<? super E>) CollectionTools.getNaturalComparator(val.getClass());
				                if (valueComparator == null) {
				                    throw new IllegalArgumentException("Elements are not comparable");
				                }
							}
						}
						if ((!next && list.size() > 0) || list.size() == chunkSize) {
						    // Maximum size of list reached, so sort it
						    // and write it out to temporary file
							list.sort(valueComparator);
							File file = writeTempFile(chunk, list);
							files.add(file);
							list.clear();
							chunk++;
						}
						if (!next) {
							break;
						}
					}
			
					// Set up temporary files for reading
					valueReaders = new ObjectInputStream[chunk];
					values = (E[]) new Object[chunk];
					//valuesReady = new boolean[chunk];
					for (int i=0; i<chunk; i++) {
						File file = files.get(i);
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);
						ObjectInputStream is = new ObjectInputStream(bis);

						// read first value from file
                        valueReaders[i] = is;
						values[i] = (E) is.readObject();
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			/**
			 * Get next value from iterators.
			 * Sets fields hasNext and next.
			 */
			void getValue() {
			    try {
    			    // index of minimum value
    				int minIdx = -1;
    				// minimum value
    				E minVal = null;
    				
    				for (int i=0; i<chunk; i++) {
    					if (valueReaders[i] != null) {
    						if (minVal == null) {
    							minVal = values[i];
    							minIdx = i;
    						} else if (valueComparator.compare(values[i], minVal) < 0) {
    							minVal = values[i];
    							minIdx = i;
    						}
    					}
    				}
    				if (minIdx == -1) {
    					hasNextValue = false;
    				} else {
                        hasNextValue = true;
                        nextValue = minVal;
    				    readValue(minIdx);
    				}
			    }
			    catch (Exception e) {
			        throw new RuntimeException(e);
			    }
			}
			
			/**
			 * Read next value from chunk.
			 * Sets fields values[chunk] and valueReaders[chunk].
			 * 
			 * @param chunk
			 * @throws Exception
			 */
			void readValue(int chunk) throws Exception {
			    try {
			        values[chunk] = (E) valueReaders[chunk].readObject();
			    }
			    catch (java.io.EOFException e) {
			        valueReaders[chunk].close();
			        valueReaders[chunk] = null;
			    }
			}

			/**
			 * Write list to temporary file.
			 * 
			 * @param chunk
			 * @param list
			 * @return
			 */
			File writeTempFile(int chunk, List<E> list) {
				ObjectOutputStream os = null;
				try {
					File temp = File.createTempFile("sort", Integer.toString(chunk));
					LOG.info("writeTempFile {}", temp.getPath());
					temp.deleteOnExit();
					FileOutputStream fos = new FileOutputStream(temp.getPath());
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					os = new ObjectOutputStream(bos);
					for (int i=0; i<list.size(); i++) {	
						os.writeObject(list.get(i));
					}
					os.close();
					return temp;
				}
				catch (IOException e) {
					throw new RuntimeException("Cannot write to temporary file", e);
				}
				finally {
					StreamTools.close(os);
				} 
			}
		};
	}
}
