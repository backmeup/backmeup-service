package org.backmeup.plugin.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilesystemLikeDatasourceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemLikeDatasourceTest.class);
    @Test
    public void testDownloadAll() throws StorageException {
        MyFilesystemLikeDatasource ds = new MyFilesystemLikeDatasource();
        MyLocalFilesystemStorage storage = new MyLocalFilesystemStorage();
        
        ds.downloadAll(null, null, storage, logProgressable);
        
        Assert.assertEquals(1, storage.getDataObjectCount());
        Assert.assertTrue(storage.getDataObjectSize() > 0);
        
        LOGGER.info("Data object size: " + storage.getDataObjectSize());
    }
    
    private class MyFilesystemLikeDatasource extends FilesystemLikeDatasource{

        @Override
        public List<FilesystemURI> list(PluginProfileDTO profile, FilesystemURI uri) {
            List<FilesystemURI> uris = new ArrayList<>();
            try {
                uris.add(new FilesystemURI(new URI("file.txt"), false));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return uris;
        }

        @Override
        public InputStream getFile(PluginProfileDTO profile, FilesystemURI uri) {
            String fileName = uri.getUri().toString();
            return getClass().getClassLoader().getResourceAsStream(fileName);
        }
        
    }
    
    private class MyLocalFilesystemStorage implements Storage {
        private int objectCount = 0;
        private long objectSize = 0;
        
        @Override
        public void open(String path) throws StorageException {
            throw new UnsupportedOperationException();
            
        }

        @Override
        public void close() throws StorageException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() throws StorageException {
            throw new UnsupportedOperationException();
            
        }

        @Override
        public int getDataObjectCount() throws StorageException {
            return objectCount;
        }

        @Override
        public long getDataObjectSize() throws StorageException {
            return objectSize;
        }

        @Override
        public Iterator<DataObject> getDataObjects() throws StorageException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsPath(String path) throws StorageException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] file = buffer.toByteArray();
                objectCount++;
                objectSize += file.length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void removeFile(String path) throws StorageException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeDir(String path) throws StorageException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void move(String fromPath, String toPath) throws StorageException {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            LOGGER.info("PROGRESS: " + message);
        }
    };
}
