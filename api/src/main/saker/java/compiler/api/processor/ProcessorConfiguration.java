package saker.java.compiler.api.processor;

import javax.annotation.processing.Processor;

import saker.java.compiler.impl.options.SimpleProcessorConfiguration;

/**
 * Represents a configuration for an {@linkplain Processor annotation processor}.
 * <p>
 * The configuration consists of a {@link ProcessorCreator} instance that defines how the processor can be instantiated
 * for the annotation processing, and other properties that define the behaviour of the processor.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * You can create a new instance using {@link #create(ProcessorCreator, boolean, boolean, boolean) create}.
 */
public interface ProcessorConfiguration {
	/**
	 * Gets the processor creator that is used to instantiate the processor.
	 * 
	 * @return The creator.
	 */
	public ProcessorCreator getCreator();

	/**
	 * Checks whether or not the processor is aggregating.
	 * <p>
	 * A processor is considered to be Aggregating if there exists an addition-wise modification to the compiled Java
	 * classes that causes it to generate different classes or resources.
	 * <p>
	 * By default, the processors should be considered to be aggregating.
	 * 
	 * @return <code>true</code> if the processor is aggregating.
	 */
	public boolean getAggregating();

	/**
	 * Checks whether or not the processor is consistent.
	 * <p>
	 * A processor is consistent if it produces the same result no matter the time of invocation. In general, if the
	 * processor generated resources based on the build time or other external untracked information, then it is not
	 * consistent.
	 * <p>
	 * By default, the processors should be considered to be consistent.
	 * 
	 * @return <code>true</code> if the processor is consistent.
	 */
	public boolean getConsistent();

	/**
	 * Checks whether or not the processor should always run.
	 * <p>
	 * If a processor is configured to always run, then it will always be invoked even if there are no deltas detected
	 * for it.
	 * <p>
	 * By default, the processors shouldn't run always.
	 * 
	 * @return <code>true</code> if the processor should always run.
	 */
	public boolean getAlwaysRun();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	/**
	 * Creates a new processor configuration with the specified properties.
	 * 
	 * @param creator
	 *            The processor creator.
	 * @param aggregating
	 *            Tells whether or not the processor is {@linkplain #getAggregating() aggregating}. Pass
	 *            <code>true</code> if you don't know.
	 * @param consistent
	 *            Tells whether or not the processor is {@linkplain #getConsistent() consistent}. Pass <code>true</code>
	 *            if you don't know.
	 * @param alwaysrun
	 *            Tells whether or not the processor should {@linkplain #getAlwaysRun() always run}. Pass
	 *            <code>false</code> if you don't know.
	 * @return The created configuration.
	 * @throws NullPointerException
	 *             If the creator is <code>null</code>.
	 */
	public static ProcessorConfiguration create(ProcessorCreator creator, boolean aggregating, boolean consistent,
			boolean alwaysrun) throws NullPointerException {
		return new SimpleProcessorConfiguration(creator, aggregating, consistent, alwaysrun);
	}

	/**
	 * Creates a new processor configuration for the specified creator, and default behavioural properties.
	 * <p>
	 * This is the same as:
	 * 
	 * <pre>
	 * {@linkplain #create(ProcessorCreator, boolean, boolean, boolean) create}(creator, true, true, false)
	 * </pre>
	 * 
	 * @param creator
	 *            The processor creator.
	 * @return The created configuration.
	 * @throws NullPointerException
	 *             If the creator is <code>null</code>.
	 */
	public static ProcessorConfiguration create(ProcessorCreator creator) throws NullPointerException {
		return create(creator, true, true, false);
	}
}