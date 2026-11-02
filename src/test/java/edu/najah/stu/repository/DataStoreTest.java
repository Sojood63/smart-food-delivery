package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.HoursFormatter;
import edu.najah.stu.domain.HoursParser;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Restaurant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataStoreTest {

    @TempDir
    Path folder;

    @Mock
    private Supplier<List<Restaurant>> firstRunData;

    private static Restaurant restaurant(String id, String hours) {
        return new Restaurant(id, "Restaurant " + id, HoursParser.parse(hours, HoursSource.MANUAL), null, Cuisine.OTHER);
    }

    // B1
    @Test
    void firstRunFillsTheFolderFromTheBundledData() {
        when(firstRunData.get()).thenReturn(List.of(restaurant("a", "Mo-Su 09:00-23:00"),
                restaurant("b", "Mo-Su 10:00-22:00")));

        FileRestaurantRepository repository = DataStore.open(folder, firstRunData);

        assertEquals(2, repository.findAll().size());
        assertTrue(Files.exists(folder.resolve(DataStore.RESTAURANTS_FILE)));
        assertTrue(Files.exists(folder.resolve(DataStore.MENU_FILE)));
    }

    // B2
    @Test
    void laterRunsKeepSavedChangesAndDoNotImportAgain() {
        when(firstRunData.get()).thenReturn(List.of(restaurant("a", "Mo-Su 09:00-23:00")));
        DataStore.open(folder, firstRunData).save(restaurant("a", "Mo-Su 12:00-14:00"));

        @SuppressWarnings("unchecked")
        Supplier<List<Restaurant>> secondRunData = mock(Supplier.class);
        FileRestaurantRepository reopened = DataStore.open(folder, secondRunData);

        verify(secondRunData, never()).get();
        assertEquals("Mo-Su 12:00-14:00",
                HoursFormatter.format(reopened.findById("a").orElseThrow().getSchedule()));
    }

    @Test
    void createsTheFolderIfMissing() {
        Path missing = folder.resolve("data");
        when(firstRunData.get()).thenReturn(List.of(restaurant("a", "Mo-Su 09:00-23:00")));

        DataStore.open(missing, firstRunData);

        assertTrue(Files.isDirectory(missing));
    }

    @Test
    void rejectsMissingArguments() {
        assertThrows(IllegalArgumentException.class, () -> DataStore.open(null, firstRunData));
        assertThrows(IllegalArgumentException.class, () -> DataStore.open(folder, null));
    }
}
