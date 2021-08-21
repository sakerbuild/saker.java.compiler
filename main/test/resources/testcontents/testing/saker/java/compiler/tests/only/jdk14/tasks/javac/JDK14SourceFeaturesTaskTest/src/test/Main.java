package test;

public class Main {
	public static void main(String[] args) {
		Object o = null;
		if(o instanceof MyClass s) {
			System.out.println("Main.main() " + s);
		}

		String html = """
	              <html>
	                  <body>
	                      <p>Hello, world</p>
	                  </body>
	              </html>
	              """;
	}
}