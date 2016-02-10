package tud.auxiliary;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public abstract class CollectionWrapper<S, T> extends AbstractCollection<T> {

	/**
	 * the collection that this wrapper is wrapping around
	 */
	private Collection<? extends S> collection;

	public CollectionWrapper(Collection<? extends S> collection) {
		this.collection = collection; 
	}
	
	public S convertTtoS(T t) {
		throw new UnsupportedOperationException();
	}

	public abstract T convertStoT(S s);
	
	@Override
	public Iterator<T> iterator() {
		final Iterator<? extends S> its = collection.iterator();
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return its.hasNext();
			}

			@Override
			public T next() {
				return convertStoT(its.next());
			}

			@Override
			public void remove() {
				its.remove();
			}
			
		};
	}

	@Override
	public int size() {
		return collection.size();
	}
	

}
