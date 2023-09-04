package test;

@MyAnnot(
	value = 123, 
	valarr = { 
		4, 
		5, 
		6, 
		6, 
	},
	nested = @SecondAnnot(
		value = 123, 
		valarr = { 
			4, 
			5, 
			6, 
			6, 
		}
	)
)
public class Main<T> implements Runnable {
	@Override
	public void run() {
	}

	public void func(
			@MyRepeatable(1)
			@MyRepeatable(1)
			int param) {

	}
}


//multiple of the same sub annotations
@MyAnnot(
	nested = {
		@SecondAnnot(
			valarr = { 
				123,
				456,
			}
		),
		@SecondAnnot(
			valarr = {  
				123,
				456,
			}
		),
	}
)
class C2 {
}

@MyRepeatable(1)
@MyRepeatable(2)
@MyRepeatable(3)
class R1{
}

//with same values
@MyRepeatable(1)
@MyRepeatable(1)
@test.MyRepeatable(1)
class R2{
}


//with same values
@MyContainer({
	@MyRepeatable(1),
	@MyRepeatable(1),
})
class R3{
}

class TPT<
	@MyRepeatable(1)
	@MyRepeatable(1)
	TypeParam> {
}

enum MyEnum {

	@MyRepeatable(1)
	@MyRepeatable(1)
	ENUM1,

	@MyRepeatable(1)
	@MyRepeatable(1)
	ENUM2;
}

