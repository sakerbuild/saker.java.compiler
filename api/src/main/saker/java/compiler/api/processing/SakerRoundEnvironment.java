package saker.java.compiler.api.processing;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;

/**
 * Describes the round environment functionality provided by the incremental Java compiler.
 * <p>
 * The {@link RoundEnvironment} instance can be downcasted to {@link SakerRoundEnvironment} during annotation
 * processing.
 * 
 * @see Processor#process(java.util.Set, RoundEnvironment)
 */
public interface SakerRoundEnvironment extends RoundEnvironment {
}
