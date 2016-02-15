///////////////////////////////////////////////////////////////////////
//                        STANFORD LOGIC GROUP                       //
//                    General Game Playing Project                   //
//                                                                   //
// Sample Player Implementation                                      //
//                                                                   //
// (c) 2007. See LICENSE and CONTRIBUTORS.                           //
///////////////////////////////////////////////////////////////////////

/**
 * 
 */
package standfordlogic.test.gdl;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import stanfordlogic.gdl.Lexer;
import stanfordlogic.gdl.SymbolTable;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class LexerTest
{
	@Test
	public void testLexer()
	{
		ByteArrayInputStream input = new ByteArrayInputStream( (new String("hello there")).getBytes() );
		SymbolTable symtab = new SymbolTable();
		
		Lexer l = new Lexer(input, symtab);
		
		assertTrue( l.token() > 255 );
		assertTrue( l.token() > 255 );
		assertEquals( -1, l.token() );
		
		assertEquals(2, symtab.size() );
	}

	@Test
	public void testLexer2()
	{
		ByteArrayInputStream input = new ByteArrayInputStream( (new String("(hello there)")).getBytes() );
		SymbolTable symtab = new SymbolTable();
		
		Lexer l = new Lexer(input, symtab);
		
		assertEquals( '(', l.token() );
		int t1 = l.token();
		int t2 = l.token();
		assertTrue( t1 > 255 );
		assertTrue( t2 > 255 );
		assertEquals( ')', l.token() );
		assertEquals( -1, l.token() );
		
		assertEquals(2, symtab.size() );
		
		assertEquals("hello", symtab.get(t1));
		assertEquals("there", symtab.get(t2));
	}

	@Test
	public void testLexer3()
	{
		ByteArrayInputStream input = new ByteArrayInputStream( (new String("(hello% there)")).getBytes() );
		SymbolTable symtab = new SymbolTable();
		
		Lexer l = new Lexer(input, symtab);
		
		// Make sure that we get an exception
		
		try {
			while ( l.token() != -1 )
				;
			assertTrue(false); // we didn't get an exception, so die
		}
		catch (RuntimeException e) {
            // ok, we got an exception.
		}
	}

	@Test
    public void testLexer4()
    {
        ByteArrayInputStream input = new ByteArrayInputStream( (new String(";; this is\n(hello there) ;; a\n;; comment\n")).getBytes() );
        SymbolTable symtab = new SymbolTable();
        
        Lexer l = new Lexer(input, symtab);
        
        assertEquals( '(', l.token() );
        int t1 = l.token();
        int t2 = l.token();
        assertTrue( t1 > 255 );
        assertTrue( t2 > 255 );
        assertEquals( ')', l.token() );
        assertEquals( -1, l.token() );
        
        assertEquals(2, symtab.size() );
        
        assertEquals("hello", symtab.get(t1));
        assertEquals("there", symtab.get(t2));
    }
	
}
