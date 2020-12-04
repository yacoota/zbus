package io.zbus.mq.disk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zbus.kit.JsonKit;
import io.zbus.mq.Protocol;
import io.zbus.mq.Protocol.MqInfo;
import io.zbus.mq.disk.support.DiskMessage;
import io.zbus.mq.disk.support.Index;
import io.zbus.mq.disk.support.QueueWriter;
import io.zbus.mq.model.ChannelReader;
import io.zbus.mq.model.MessageQueue.AbstractMessageQueue;
import io.zbus.transport.Message;

public class DiskQueue extends AbstractMessageQueue {
	private static final Logger logger = LoggerFactory.getLogger(DiskQueue.class); 
	final Index index;     
	private final QueueWriter writer;   
	
	public DiskQueue(String mqDir, File baseDir, String creator) throws IOException { 
		super(FileNameNormalizer.normalizeName(mqDir)); 
		File mqDirFile = new File(baseDir, FileNameNormalizer.escapeName(mqDir));
		index = new Index(mqDirFile);
		if(creator != null) {
			index.setCreator(creator); 
		}
		writer = new QueueWriter(index);
		
		loadChannels();
	}  
	
	public DiskQueue(String mqDir, File baseDir) throws IOException { 
		this(mqDir,  baseDir, null);
	}  
	 
	@Override
	public MqInfo info() {
		MqInfo info = new MqInfo();
		info.name = name();
		info.type = type();
		info.mask = getMask();
		info.messageDepth = size(); 
		info.channelList = new ArrayList<>(channels().values()); 
		info.creator = index.getCreator();
		info.createdAt = index.getCreatedTime();
		return info;
	}
	
	@Override
	public String type() { 
		return Protocol.DISK;
	}
	
	@Override
	public long size() { 
		return index.getMessageCount();
	}
	
	@Override
	protected ChannelReader buildChannelReader(String channelId) throws IOException {
		return new DiskChannelReader(channelId, this);
	}
	
	private void loadChannels() {
		File[] channelFiles = index.getReaderDir().listFiles( pathname-> {
			return Index.isReaderFile(pathname); 
		});
        if (channelFiles != null && channelFiles.length> 0) {
            for (File channelFile : channelFiles) {  
            	String channelName = channelFile.getName();
            	channelName = channelName.substring(0, channelName.lastIndexOf('.'));   
            	channelName = FileNameNormalizer.normalizeName(channelName);
				try {
					ChannelReader reader = buildChannelReader(channelName);
					channelTable.put(channelName, reader);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				} 
            }
        } 
	}
	 
	private DiskMessage diskMessage(Message message) {
		DiskMessage diskMsg = new DiskMessage();
		diskMsg.id = (String)message.getHeader(Protocol.ID);
		diskMsg.tag = (String)message.getHeader(Protocol.FILTER);
		diskMsg.body = JsonKit.toJSONBytes(message, "UTF8");
		return diskMsg;
	}
	@Override
	public void write(Message message) { 
		try {  
			writer.write(diskMessage(message)); 
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} 
	} 
	
	@Override
	public void write(List<Message> messages) {
		try { 
			DiskMessage[] diskMsgs = new DiskMessage[messages.size()];
			for(int i=0;i<messages.size();i++) { 
				diskMsgs[i] = diskMessage(messages.get(i)); 
			} 
			writer.write(diskMsgs);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} 
	} 

	@Override
	public Integer getMask() {
		return index.getMask(); 
	}
	
	@Override
	public void setMask(Integer mask) { 
		if(mask == null) return;
		index.setMask(mask);
	}

	@Override
	public void flush() { 
		
	}
	
	@Override
	public void destroy() { 
		try {
			writer.close();
			for(ChannelReader reader : channelTable.values()) {
				reader.destroy();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		try {
			index.delete();
		} catch (IOException e) { 
			logger.error(e.getMessage(), e);
		} 
	} 
}
