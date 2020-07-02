public record PublicWithProtectedConstructor(long j) {
	protected PublicWithProtectedConstructor(int i){
		this((long)i);
	}
}