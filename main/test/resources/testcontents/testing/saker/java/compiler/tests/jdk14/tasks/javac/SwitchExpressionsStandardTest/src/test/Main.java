package test;

import javax.lang.model.element.ElementKind;

/**
 * from https://openjdk.java.net/jeps/361
 */
public class Main {
	public static void main(String[] args) {
	}

	public static void motivation1(WeekDay day) {
		switch (day) {
			case MONDAY:
			case FRIDAY:
			case SUNDAY:
				System.out.println(6);
				break;
			case TUESDAY:
				System.out.println(7);
				break;
			case THURSDAY:
			case SATURDAY:
				System.out.println(8);
				break;
			case WEDNESDAY:
				System.out.println(9);
				break;
		}
	}
	public static void motivation2(WeekDay day) {
		switch (day) {
		    case MONDAY, FRIDAY, SUNDAY -> System.out.println(6);
		    case TUESDAY                -> System.out.println(7);
		    case THURSDAY, SATURDAY     -> System.out.println(8);
		    case WEDNESDAY              -> System.out.println(9);
		}
	}

	public static void motivation3(WeekDay day) {
		switch (day) {
		    case MONDAY:
		    case TUESDAY:
		        int temp = 0;//...     // The scope of 'temp' continues to the }
		        break;
		    case WEDNESDAY:
		    case THURSDAY:
		        int temp2 = 0;//...    // Can't call this variable 'temp'
		        break;
		    default:
		        int temp3 = 0;//...    // Can't call this variable 'temp'
		}
	}

	public static void motivation4(WeekDay day) {
		int numLetters;
		switch (day) {
		    case MONDAY:
		    case FRIDAY:
		    case SUNDAY:
		        numLetters = 6;
		        break;
		    case TUESDAY:
		        numLetters = 7;
		        break;
		    case THURSDAY:
		    case SATURDAY:
		        numLetters = 8;
		        break;
		    case WEDNESDAY:
		        numLetters = 9;
		        break;
		    default:
		        throw new IllegalStateException("Wat: " + day);
		}
	}
	
	static void howMany(int k) {
	    switch (k) {
	        case 1  -> System.out.println("one");
	        case 2  -> System.out.println("two");
	        default -> System.out.println("many");
	    }
	}
	
	static void howMany2(int k) {
	    System.out.println(
	        switch (k) {
	            case  1 -> "one";
	            case  2 -> "two";
	            default -> "many";
	        }
	    );
	}
	
	private void yield(WeekDay day) {
		int j = switch (day) {
		    case MONDAY  -> 0;
		    case TUESDAY -> 1;
		    default      -> {
		        int k = day.toString().length();
		        int result = k;
		        yield result;
		    }
		};
	}
	
	private void yield2(String s) {
		int result = switch (s) {
		    case "Foo": 
		        yield 1;
		    case "Bar":
		        yield 2;
		    default:
		        System.out.println("Neither Foo nor Bar, hmmm...");
		        yield 0;
		};
	}
	
	private void exhaustiveness(WeekDay day) {
		int i = switch (day) {
		    case MONDAY -> {
		        System.out.println("Monday"); 
		        // -fixed for test- ERROR! Block doesn't contain a yield statement
		        yield 0;
		    }
		    default -> 1;
		};
		i = switch (day) {
		    case MONDAY, TUESDAY, WEDNESDAY: 
		        yield 0;
		    default: 
		        System.out.println("Second half of the week");
		        // -fixed for test- ERROR! Group doesn't contain a yield statement
		        yield 1;
		};
	}

	public static void motivation5(WeekDay day) {
		int numLetters = switch (day) {
		    case MONDAY, FRIDAY, SUNDAY -> 6;
		    case TUESDAY                -> 7;
		    case THURSDAY, SATURDAY     -> 8;
		    case WEDNESDAY              -> 9;
		};
	}

	enum WeekDay {
		MONDAY,
		TUESDAY,
		WEDNESDAY,
		THURSDAY,
		FRIDAY,
		SATURDAY,
		SUNDAY;
	}
}