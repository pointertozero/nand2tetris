import java.util.Hashtable;

public class SymbolTable {
	private Hashtable<String, String[]> classScope;
	private Hashtable<String, String[]> subroutineScope;
	
	// indices constants for String[] in Hashtables
	private final int TYPE = 0;
	private final int KIND = 1;
	private final int INDEX = 2;
	
	public SymbolTable() {
		classScope = new Hashtable<String, String[]>();
		subroutineScope = new Hashtable<String, String[]>();
	}
	
	public void startSubroutine() {
		subroutineScope.clear();
	}
	
	public void define(String name, String type, String kind) {
		// kind ( STATIC ,FIELD , ARG , or VAR )
		
		if (kind.equals("STATIC")
			|| kind.equals("FIELD"))
		{
			classScope.put(name, new String[] { type, kind, String.valueOf(varCount(kind)) });
		} else if (kind.equals("VAR")
					|| kind.equals("ARG"))
		{
			subroutineScope.put(name, new String[] { type, kind, String.valueOf(varCount(kind)) });
		} else {
			throw new Error("wrong kind");
		}
	}
	
	public int varCount(String kind) {
		int cnt = 0;
		
		if (kind.equals("STATIC")
			|| kind.equals("FIELD"))
		{
			// search inside of classScope
			for (String[] a : classScope.values()) {
				if (a[KIND].equals(kind)) {
					++cnt;
				}
			}
		} else if (kind.equals("ARG")
					|| kind.equals("VAR"))
		{
			// search inside of subroutineScope
			for (String[] a : subroutineScope.values()) {
				if (a[KIND].equals(kind)) {
					++cnt;
				}
			}
		} else {
			throw new Error("wrong kind");
		}
		
		
		return cnt;
	}
	
	public String kindOf(String name) {
		String[] TypeKindIndex = findTypeKindIndex(name);
		
		// returns kind of identifier
		// or null if not defined in the scope
		// (subroutine or class name)
		
		if (TypeKindIndex == null) {
			return null;
		} else {
			return TypeKindIndex[KIND];
		}
	}
	
	public String typeOf(String name) {
		// assumes that the kindOf(name) doesn't return null?
		assert kindOf(name) != null;
		
		String[] TypeKindIndex = findTypeKindIndex(name);
		
		return TypeKindIndex[TYPE];
	}
	
	public int indexOf(String name) {
		// assumes that the kindOf(name) doesn't return null?
		assert kindOf(name) != null;
		
		String[] TypeKindIndex = findTypeKindIndex(name);
		
		return Integer.parseInt(TypeKindIndex[INDEX]);
	}
	
	public String[] findTypeKindIndex(String key) {
		// search in both scopes, check what is returned if not found inside
		
		// throw new Error("to implement");
		
		String[] TypeKindIndex = subroutineScope.get(key);
		if (TypeKindIndex == null) {
			// not found in subroutineScope
			// search in classScope
			TypeKindIndex = classScope.get(key);
		}
		
		return TypeKindIndex;
	}
	
	public String getSegmentName(String varName) {
		
		switch (kindOf(varName)) {
		case "STATIC":
			return "static";
		case "FIELD":
			return "this";
		case "ARG":
			return "argument";
		case "VAR":
			return "local";
		default:
			throw new Error();
		}
	}
}
