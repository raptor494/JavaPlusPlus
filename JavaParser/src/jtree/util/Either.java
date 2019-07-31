package jtree.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.NonNull;

@SuppressWarnings("unchecked")
public final class Either<F,S> {
    public static <F,S> Either<F,S> first(@NonNull F first) {
        return new Either<>(true, first);
    }
    
    public static <F,S> Either<F,S> second(@NonNull S second) {
        return new Either<>(false, second);
    }
    
    private final boolean isFirst;
    private final Object value;
    
    private Either(boolean isFirst, Object value) {
        if(this.isFirst = isFirst) {
            this.value = value;
        } else {
            this.value = value;
        }
    }
    
    public boolean isFirst() { return isFirst; }
    public boolean isSecond() { return !isFirst; }
    
    public F first() {
        if(isFirst) {
			return (F)value;
		} else {
			throw new NoSuchElementException();
		}
    }
    
    public F firstOrElse(F other) {
        return isFirst? (F)value : other;
    }
    
    public F firstOrElseGet(Supplier<? extends F> supplier) {
        return isFirst? (F)value : supplier.get();
    }
    
    public F firstOrElseThrow() {
        if(isFirst) {
			return (F)value;
		} else {
			throw new NoSuchElementException();
		}
    }
    
    public <X extends Throwable> F firstOrElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if(isFirst) {
			return (F)value;
		} else {
			throw exceptionSupplier.get();
		}
    }
    
    public S second() {
        if(isFirst) {
			throw new NoSuchElementException();
		} else {
			return (S)value;
		}
    }
    
    public S secondOrElse(S other) {
        return isFirst? other : (S)value;
    }
    
    public S secondOrElseGet(Supplier<? extends S> supplier) {
        return isFirst? supplier.get() : (S)value;
    }
    
    public S secondOrElseThrow() {
        if(isFirst) {
			throw new NoSuchElementException();
		} else {
			return (S)value;
		}
    }
    
    public <X extends Throwable> S secondOrElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if(isFirst) {
			throw exceptionSupplier.get();
		} else {
			return (S)value;
		}
    }
    
    public Object getValue() {
        return value;
    }
    
    public void ifFirst(Consumer<? super F> action) {
        if(isFirst) {
			action.accept((F)value);
		}
    }
    
    public void ifSecond(Consumer<? super S> action) {
        if(!isFirst) {
			action.accept((S)value);
		}
    }
    
    public void accept(Consumer<? super F> action, Consumer<? super S> otherAction) {
        if(isFirst) {
			action.accept((F)value);
		} else {
			otherAction.accept((S)value);
		}
    }
    
    public <F2,S2> Either<F2,S2> flatMap(Function<? super F, ? extends Either<? extends F2, ? extends S2>> firstMapper, Function<? super S, ? extends Either<? extends F2, ? extends S2>> secondMapper) {
        return isFirst? (Either<F2,S2>)firstMapper.apply((F)value) : (Either<F2,S2>)secondMapper.apply((S)value);
    }
    
    public <F2,S2> Either<F2,S2> flatMap(BiFunction<? super Boolean, ? super Object, ? extends Either<? extends F2, ? extends S2>> mapper) {
        return (Either<F2,S2>)mapper.apply(isFirst, value);
    }
    
    public <F2,S2> Either<F2,S2> map(BiFunction<? super Boolean, ? super Object, ?> mapper) {
        Object result = mapper.apply(isFirst, value);
        return isFirst? Either.first((F2)result) : Either.second((S2)result);
    }
    
    public <F2,S2> Either<F2,S2> map(Function<? super F, ? extends F2> firstMapper, Function<? super S, ? extends S2> secondMapper) {
    	return isFirst? Either.first(firstMapper.apply((F)value)) : Either.second(secondMapper.apply((S)value));
    }
    
    public <T> T unravel(Function<? super F, ? extends T> firstMapper, Function<? super S, ? extends T> secondMapper) {
    	return isFirst? firstMapper.apply((F)value) : secondMapper.apply((S)value);
    }
    
    public <T> T unravel(BiFunction<? super Boolean, ? super Object, ? extends T> func) {
    	return func.apply(isFirst, value);
    }
    
    @Override
	public int hashCode() {
        return Objects.hash(isFirst, value);
    }
    
    @Override
	public boolean equals(Object obj) {
        if(this == obj) {
			return true;
		}
        if(obj instanceof Either) {
            var either = (Either<?,?>)obj;
            return isFirst == either.isFirst && Objects.equals(value, either.value);
        }
        return false;
    }
    
    @Override
	public String toString() {
        return String.format("Either.%s(%s)", isFirst? "first" : "second", value);
    }
}
	
