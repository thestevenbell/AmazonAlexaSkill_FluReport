package test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.amazon.speech.speechlet.State;

import main.flureport.States;

public class StateEnumTest {

	@Test
	public void StateEnumTest() {
		States alabama = States.parse("AL");
		assertEquals("ALABAMA",States.ALABAMA.toString() );
	}

}
