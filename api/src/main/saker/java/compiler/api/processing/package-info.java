/**
 * Contains classes that are used by the incremental Java compiler during annotation processing.
 * <p>
 * The interfaces can be downcasted to their API types when appropriate. (E.g. {@link ProcessingEnvironment} that a
 * {@link Processor} receives may be downcasted to {@link SakerProcessingEnvironment}.) When doing so, make sure to use
 * <code>instanceof</code> to ensure that your processor doesn't cause a cast exception when being run in a different
 * build environment.
 */
package saker.java.compiler.api.processing;
