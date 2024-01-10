package org.web3j.protocol.core.methods.response;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;

public class AxiomStatus extends Response<AxiomStatus.Status> {

    @Override
    @JsonDeserialize(using = AxiomStatus.ResponseDeserialiser.class)
    public void setResult(AxiomStatus.Status result) {
        super.setResult(result);
    }

    public AxiomStatus.Status getStatus() {
        return getResult();
    }

    public static class Status {
        private String status;

        public Status() {}

        public Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ResponseDeserialiser extends JsonDeserializer<AxiomStatus.Status> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public AxiomStatus.Status deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(jsonParser, AxiomStatus.Status.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
