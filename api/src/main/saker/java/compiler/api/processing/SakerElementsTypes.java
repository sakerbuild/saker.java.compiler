package saker.java.compiler.api.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Describes the {@link Element} and {@link TypeMirror} functionality provided by the incremental Java compiler.
 * <p>
 * The {@link Types} and {@link Elements} instances can be downcasted to {@link SakerElementsTypes} during annotation
 * processing.
 * 
 * @see ProcessingEnvironment#getElementUtils()
 * @see ProcessingEnvironment#getTypeUtils()
 */
public interface SakerElementsTypes extends Elements, Types {
}
