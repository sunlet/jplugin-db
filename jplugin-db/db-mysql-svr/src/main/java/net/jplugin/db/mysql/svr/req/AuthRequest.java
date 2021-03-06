package net.jplugin.db.mysql.svr.req;

import static net.jplugin.db.mysql.svr.consts.CapabilityFlags.*;

import java.util.Map;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.jplugin.db.mysql.svr.utils.IOUtils;

@Getter
public class AuthRequest extends AbstractPackedRequest {

    /**
     * Capability see https://dev.mysql.com/doc/internals/en/capability-flags.html
     */
    private int clientCapability;

    /**
     * Package max size
     */
    private int maxPackageLength;


    private byte charSet;

    /**
     * Padding
     */
    private String unused;

    private String userName;

    private String authResponse;
    
    private byte[] password;

    private String database;

    private String authPluginName;

    private Map<String, String> attributes = Maps.newHashMap();

    @Override
    public void readContent(ByteBuf byteBuf) {
        this.clientCapability = IOUtils.readInteger(byteBuf, 4);
        this.maxPackageLength = IOUtils.readInteger(byteBuf, 4);
        this.charSet = IOUtils.readByte(byteBuf);

        //23 byte is useless
        this.unused = IOUtils.readFixLengthString(byteBuf, 23);

        this.userName = IOUtils.readString(byteBuf);

        if (0 != (clientCapability & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA)) {
//            authResponse = IOUtils.readLengthEncodedString(byteBuf);
            
            password = IOUtils.readLengthBytesArray(byteBuf);
        } else if (0 != (clientCapability & CLIENT_SECURE_CONNECTION)) {
            int length = IOUtils.readLengthEncodedInteger(byteBuf);
//            authResponse = IOUtils.readFixLengthString(byteBuf, length);
            
            //密码保存到bytes。 2022-3-10
            password = IOUtils.readBytes(byteBuf, length);
            
            
        } else {
//            authResponse = IOUtils.readString(byteBuf);
            
            password = IOUtils.readBytes(byteBuf);
        }

        if (0 != (clientCapability & CLIENT_CONNECT_WITH_DB)) {
            database = IOUtils.readString(byteBuf);
        }

        if (0 != (clientCapability & CLIENT_PLUGIN_AUTH)) {
            authPluginName = IOUtils.readString(byteBuf);
        }

        if (byteBuf.isReadable()) {
            if (0 != (clientCapability & CLIENT_CONNECT_ATTRS)) {
                int attrSize = IOUtils.readLengthEncodedInteger(byteBuf);
                for (int i = 0; i < attrSize; i++) {
                    attributes.put(
                            IOUtils.readLengthEncodedString(byteBuf),
                            IOUtils.readLengthEncodedString(byteBuf)
                    );
                }
            }
        }
    }
}
