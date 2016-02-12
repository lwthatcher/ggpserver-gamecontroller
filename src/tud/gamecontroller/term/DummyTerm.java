package tud.gamecontroller.term;

import java.util.List;

/**
 * 
 * This is a dummy term that represents a constant "dummy" no matter what the underlying term is.
 * 
 * This class is useful if you just need a class that implements TermInterface, but does not depend on anything.
 *
 */
public class DummyTerm extends AbstractTerm<DummyTerm, Object> {

	public DummyTerm(Object nativeTerm) {
		super(nativeTerm);
	}

	@Override
	public List<? extends DummyTerm> getArgs() {
		return null;
	}

	@Override
	public String getName() {
		return "dummy";
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public boolean isVariable() {
		return false;
	}

}
