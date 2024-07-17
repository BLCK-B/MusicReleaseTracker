import com.blck.MusicReleaseTracker.Core.ValueStore;
import com.blck.MusicReleaseTracker.StartSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StartSetupTest {

    @Mock
    ValueStore store;
    @InjectMocks
    StartSetup startSetup;

    @Captor
    ArgumentCaptor<String> stringCaptor;

    @Test
    void ensureDBpathIntegrity() {
        String DBpath = File.separator + "MusicReleaseTracker" + File.separator + "musicdata.db";

        startSetup.createPaths();

        verify(store).setDBpath(stringCaptor.capture());
        assertTrue(stringCaptor.getValue().contains(DBpath));
        assertTrue(stringCaptor.getValue().contains("jdbc:sqlite:"));
    }

}
