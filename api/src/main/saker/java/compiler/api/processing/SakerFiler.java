package saker.java.compiler.api.processing;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Describes the {@link Filer} functionality provided by the incremental Java compiler.
 * <p>
 * The {@link Filer} instance can be downcasted to {@link SakerFiler} during annotation processing.
 * 
 * @see ProcessingEnvironment#getFiler()
 */
public interface SakerFiler extends Filer {

}
