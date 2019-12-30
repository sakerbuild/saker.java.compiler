package saker.java.compiler.api.compile;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import saker.java.compiler.api.processor.ProcessorConfiguration;

/**
 * Represents a configuration for an annotation processor.
 * <p>
 * The interface holds information about how the processor should be instantiated, its behavioural properties, and other
 * runtime information for its invocation.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link JavaAnnotationProcessorBuilder} to create a new instance.
 */
public interface JavaAnnotationProcessor {
	/**
	 * Gets the processor configuration.
	 * <p>
	 * The configuration specifies the basic properties of the processor and how it should be instantiated.
	 * 
	 * @return The configuration.
	 */
	public ProcessorConfiguration getProcessor();

	/**
	 * Checks whether or not the processor is aggregating.
	 * <p>
	 * This method takes the {@link ProcessorConfiguration#getAggregating()} value into account, and may override it
	 * with an user specified configuration.
	 * 
	 * @return <code>true</code> if the processor is aggregating.
	 */
	public boolean getAggregating();

	/**
	 * Checks whether or not the processor is consistent.
	 * <p>
	 * This method takes the {@link ProcessorConfiguration#getConsistent()} value into account, and may override it with
	 * an user specified configuration.
	 * 
	 * @return <code>true</code> if the processor is consistent.
	 */
	public boolean getConsistent();

	/**
	 * Checks whether or not the processor should always run.
	 * <p>
	 * This method takes the {@link ProcessorConfiguration#getAlwaysRun()} value into account, and may override it with
	 * an user specified configuration.
	 * 
	 * @return <code>true</code> if the processor should always run.
	 */
	public boolean getAlwaysRun();

	/**
	 * Gets the key-value options that should be passed to the annotation processor.
	 * <p>
	 * These options are private to the processor and added to the
	 * {@linkplain JavaCompilationTaskBuilder#setAnnotationProcessorOptions(Map) global options}. The options are
	 * available to the processor by calling {@link ProcessingEnvironment#getOptions()}.
	 * 
	 * @return The options for the processor.
	 */
	public Map<String, String> getOptions();

	/**
	 * Gets the warning types that should be suppressed for this processor.
	 * <p>
	 * These warning types are private to the processor, and added to the
	 * {@linkplain JavaCompilationTaskBuilder#setSuppressWarnings(Collection) global suppress warnings}.
	 * 
	 * @return The warning types.
	 * @see JavaCompilerWarningType
	 */
	public Collection<String> getSuppressWarnings();

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();
}
