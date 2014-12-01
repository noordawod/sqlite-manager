/**
 * This file is part of SQLite Manager library. Copyright (C) 2014 Noor Dawod. All
 * rights reserved. https://github.com/noordawod/sqlite-manager
 *
 * Released under the MIT license http://en.wikipedia.org/wiki/MIT_License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.fine47.sqlite.aux;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A robust {@link ArrayList} implementation which supports adding elements at
 * any position, even beyond the end of the list.
 *
 * @param <E> Element type of this list
 * @since 1.0
 */
public class ElasticArrayList<E> extends ArrayList<E> {

  /**
   * Default constructor.
   */
  public ElasticArrayList() {
    super();
  }

  /**
   * Constructs a new instance of {@link ElasticArrayList} with the specified
   * initial capacity.
   *
   * @param capacity initial list capacity
   */
  public ElasticArrayList(int capacity) {
    super(capacity);
  }

  /**
   * Constructs a new instance of {@link ElasticArrayList} containing the
   * elements of the specified collection.
   *
   * @param collection of elements to add
   */
  public ElasticArrayList(Collection<? extends E> collection) {
    super(collection);
  }

  /**
   * Adds an entry at the specified index position. No exceptions are thrown if
   * the index is outside the array bounds, instead NULLs are inserted up to the
   * index position.
   *
   * @param index at which to add the entry
   * @param entry to add
   */
  @Override
  public void add(int index, E entry) {
    if(-1 < index) {
      int delta = index - size();
      while(0 < delta--) {
        super.add(null);
      }
      super.add(entry);
    }
  }

  /**
   * Returns the entry at the specified index position. No exceptions are thrown
   * if the index is outside the array bounds, instead NULL is returned.
   *
   * @param index at which to retrieve the entry
   * @return entry at the index if valid, NULL otherwise
   */
  @Override
  public E get(int index) {
    return -1 < index && size() > index ? super.get(index) : null;
  }
}
