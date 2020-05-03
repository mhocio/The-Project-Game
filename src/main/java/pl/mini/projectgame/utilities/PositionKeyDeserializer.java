package pl.mini.projectgame.utilities;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import pl.mini.projectgame.models.Position;

import java.io.IOException;

public class PositionKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        return new Position(key);
    }
}