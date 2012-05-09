package com.continuuity.common.utils;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * An {@link ImmutablePair} consists of two elements within. The elements once set
 * in the ImmutablePair cannot be modified. The class itself is final, so that it
 * cannot be subclassed. This is general norm for creating Immutable classes.
 * Please note that the {@link ImmutablePair} cannot be modified once set, but the
 * objects within them can be, so in general it means that if there are mutable objects
 * within the pair then the pair itself is effectively mutable.
 *
 * <code>
 *   ImmutablePair<Tuple, TupleInputStreamIdentifier> tupleStreamPair= new
 *    ImmutablePair<Tuple, TupleInputStreamIdentifier> (tuple, identifier);
 *   ...
 *   ...
 *   Tuple t = tupleStreamPair.getFirst();
 *   TupleInputStreamIdentifier identifier = tupleStreamPair.getSecond();
 *   ...
 * </code>
 */
public final class ImmutablePair<A, B> {
  private final A first;
  private final B second;

  public ImmutablePair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
            .add("first", first)
            .add("second", second)
            .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(first, second);
  }

  @Override
  public boolean equals(Object o) {
    if(o == null) return false;
    ImmutablePair<A,B> other = (ImmutablePair) o;
    return Objects.equal(first, other.first) && Objects.equal(second, other.second);
  }

}
