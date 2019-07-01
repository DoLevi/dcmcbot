package de.untenrechts.dev.dcmcbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import java.io.Reader;

public class DcMcBotConfigHandler {

    private static Logger LOG = LoggerFactory.getLogger(DcMcBotConfigHandler.class);

    private static final DcMcBotConfigHandler INSTANCE = new DcMcBotConfigHandler();
    private DcMcBotConfigType config;


    private DcMcBotConfigHandler() {
        this.config = null;
    }

    public static void initialize(Reader configReader) {
        if (getConfig() == null) {
            try {
                Unmarshaller unmarshaller
                        = JAXBContext.newInstance(ObjectFactory.class).createUnmarshaller();
                Object configObject
                        = JAXBIntrospector.getValue(unmarshaller.unmarshal(configReader));
                INSTANCE.config = (DcMcBotConfigType) configObject;
            } catch (JAXBException e) {
                throw new IllegalStateException(e);
            }
        } else {
            LOG.warn("Ignoring attempted double initialization of {}.",
                    DcMcBotConfigHandler.class.getSimpleName());
        }
    }

    public static DcMcBotConfigType getConfig() {
        return INSTANCE.config;
    }
}
