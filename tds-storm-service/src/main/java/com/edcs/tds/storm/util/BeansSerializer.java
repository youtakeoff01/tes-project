package com.edcs.tds.storm.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edcs.tds.storm.model.TestingResultData;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BeansSerializer {

	private Kryo kryo = null;

	private Output output = null;

	public BeansSerializer() {
		super();
		this.kryo = new Kryo();
		//
		this.kryo.register(Date.class);
		this.kryo.register(BigDecimal.class);
		this.kryo.register(Map.class);
		this.kryo.register(HashMap.class);
		this.kryo.register(List.class);
		this.kryo.register(Integer.class);
		this.kryo.register(Long.class);
		this.kryo.register(TestingResultData.class);
		this.kryo.register(Timestamp.class);

		//
		this.output = new Output(1024, 1024 * 1024);
	}

	public synchronized byte[] asByteArray(Object object) {
		output.clear();
		kryo.writeClassAndObject(output, object);
		output.close();
		return output.toBytes();
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T asObject(byte[] b, Class<T> type) {
		Input input = new Input(b);
		return (T) kryo.readClassAndObject(input);
	}

	public synchronized Object asObject(byte[] b) {
		Input input = new Input(b);
		return kryo.readClassAndObject(input);
	}
}
