package it;

import java.io.File;

import net.sf.lightairmp.GenerateXsdMojo;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class PluginTest {

	@Test
	public void test() throws MojoFailureException {
		final GenerateXsdMojo mojo = new GenerateXsdMojo();
		mojo.setXsdDir(new File("target/data/gogo"));
		mojo.execute();
	}

}
