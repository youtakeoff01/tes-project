package com.edcs.tds.storm.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edcs.tds.storm.core.exception.GroovyException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class ScriptExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ScriptExecutor.class);

	private static GroovyShell groovyShell;

	public static synchronized GroovyShell getDefaultShell() {
		if (groovyShell == null) {
			groovyShell = new GroovyShell(new Binding());
		}
		return groovyShell;
	}

	public static synchronized GroovyShell getShell(Binding binding) {
		if (groovyShell == null) {
			groovyShell = new GroovyShell(binding);
		}
		return groovyShell;
	}

	public String execute(Script obj) throws GroovyException {
		String result = null;
		try {
			if (obj != null) {
				Object res = obj.run();
				if (res != null)
					result = String.valueOf(res);
			}
		} catch (Exception e) {
			logger.error("Execute script error: " + e);
			// throw new GroovyException(e);
		}
		return result;
	}

}
