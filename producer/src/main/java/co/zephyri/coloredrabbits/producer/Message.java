package co.zephyri.coloredrabbits.producer;

import java.io.Serializable;

public record Message(String batchId, String text, int sleep) implements Serializable {}