/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.leshan.core.CertificateUsage;
import org.eclipse.leshan.core.SecurityMode;
import org.eclipse.leshan.core.request.BindingMode;

/**
 * A client configuration to apply to a device during a bootstrap session.
 * <p>
 * Configuration contains :
 * <ul>
 * <li>a List of LWM2M path to delete. For each path a Bootstrap Delete Request will be sent.
 * <li>a Map from instanceId to Instance value for Server object. For each entry a Bootstrap Write Request will be sent.
 * <li>a Map from instanceId to Instance value for Security object. For each entry a Bootstrap Write Request will be
 * sent.
 * <li>a Map from instanceId to Instance value for ACL object. For each entry a Bootstrap Write Request will be sent.
 * </ul>
 * 
 * @see BootstrapConfigStore
 * @see DefaultBootstrapHandler
 */
public class BootstrapConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * List of LWM2M path to delete.
     */
    public List<String> toDelete = new ArrayList<>();

    /**
     * Map indexed by Server Instance Id. Key is the Server Instance to write.
     */
    public Map<Integer, ServerConfig> servers = new HashMap<>();

    /**
     * Map indexed by Security Instance Id. Key is the Server Instance to write.
     */
    public Map<Integer, ServerSecurity> security = new HashMap<>();

    /**
     * Map indexed by ACL Instance Id. Key is the ACL Instance to write.
     */
    public Map<Integer, ACLConfig> acls = new HashMap<>();

    /** Server Configuration (object 1) as defined in LWM2M 1.0.x TS. */
    public static class ServerConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        /** Used as link to associate server Object Instance. */
        public int shortId;
        /** Specify the lifetime of the registration in seconds (see Section 5.3 Registration). */
        public int lifetime = 86400;
        /**
         * The default value the LwM2M Client should use for the Minimum Period of an Observation in the absence of this
         * parameter being included in an Observation. If this Resource doesn’t exist, the default value is 0.
         */
        public Integer defaultMinPeriod = 1;
        /**
         * The default value the LwM2M Client should use for the Maximum Period of an Observation in the absence of this
         * parameter being included in an Observation.
         */
        public Integer defaultMaxPeriod = null;
        /**
         * If this Resource is executed, this LwM2M Server Object is disabled for a certain period defined in the
         * Disabled Timeout Resource. After receiving “Execute” operation, LwM2M Client MUST send response of the
         * operation and perform de-registration process, and underlying network connection between the Client and
         * Server MUST be disconnected to disable the LwM2M Server account. After the above process, the LwM2M Client
         * MUST NOT send any message to the Server and ignore all the messages from the LwM2M Server for the period.
         */
        public Integer disableTimeout = null;
        /**
         * If true, the LwM2M Client stores “Notify” operations to the LwM2M Server while the LwM2M Server account is
         * disabled or the LwM2M Client is offline. After the LwM2M Server account is enabled or the LwM2M Client is
         * online, the LwM2M Client reports the stored “Notify” operations to the Server. If false, the LwM2M Client
         * discards all the “Notify” operations or temporarily disables the Observe function while the LwM2M Server is
         * disabled or the LwM2M Client is offline. The default value is true. The maximum number of storing
         * Notifications per Server is up to the implementation.
         */
        public boolean notifIfDisabled = true;
        /**
         * This Resource defines the transport binding configured for the LwM2M Client. If the LwM2M Client supports the
         * binding specified in this Resource, the LwM2M Client MUST use that transport for the Current Binding Mode.
         */
        public EnumSet<BindingMode> binding = EnumSet.of(BindingMode.U);

        @Override
        public String toString() {
            return String.format(
                    "ServerConfig [shortId=%s, lifetime=%s, defaultMinPeriod=%s, defaultMaxPeriod=%s, disableTimeout=%s, notifIfDisabled=%s, binding=%s]",
                    shortId, lifetime, defaultMinPeriod, defaultMaxPeriod, disableTimeout, notifIfDisabled, binding);
        }
    }

    /**
     * Security Configuration (object 0) as defined in LWM2M 1.0.x TS.
     * <p>
     * This LwM2M Object provides the keying material of a LwM2M Client appropriate to access a specified LwM2M Server.
     * <p>
     * One Object Instance SHOULD address a LwM2M Bootstrap-Server. These LwM2M Object Resources MUST only be changed by
     * a LwM2M Bootstrap-Server or Bootstrap from Smartcard and MUST NOT be accessible by any other LwM2M Server.
     */
    public static class ServerSecurity implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Uniquely identifies the LwM2M Server or LwM2M Bootstrap-Server. The format of the CoAP URI is defined in
         * Section 6 of RFC 7252.]]
         */
        public String uri;
        /**
         * Determines if the current instance concerns a LwM2M Bootstrap-Server (true) or a standard LwM2M Server
         * (false).
         */
        public boolean bootstrapServer = false;
        /**
         * Determines which UDP payload security mode is used
         * <ul>
         * <li>0: Pre-Shared Key mode
         * <li>1: Raw Public Key mode
         * <li>2: Certificate mode
         * <li>3: NoSec mode
         * <li>4: Certificate mode with EST
         * </ul>
         */
        public SecurityMode securityMode;
        /**
         * Stores the LwM2M Client’s Certificate (Certificate mode), public key (RPK mode) or PSK Identity (PSK mode).
         * <p>
         * The format is defined in Section E.1.1 of the LwM2M version 1.0 specification.
         */
        public byte[] publicKeyOrId = new byte[] {};
        /**
         * Stores the LwM2M Server’s or LwM2M Bootstrap-Server’s Certificate (Certificate mode), public key (RPK mode).
         * <p>
         * The format is defined in Section E.1.1 of the LwM2M version 1.0 specification.
         */
        public byte[] serverPublicKey = new byte[] {};
        /**
         * Stores the secret key or private key of the security mode.
         * <p>
         * The format of the keying material is defined by the security mode in Section E.1.1 of the LwM2M version 1.0
         * specification.
         * <p>
         * This Resource MUST only be changed by a bootstrap-server and MUST NOT be readable by any server.
         */
        public byte[] secretKey = new byte[] {};
        /**
         * Determines which SMS security mode is used (see section 7.2 of the LwM2M version 1.0 specification)
         * <ul>
         * <li>0: Reserved for future use
         * <li>1: DTLS mode (Device terminated) PSK mode assumed
         * <li>2: Secure Packet Structure mode (Smartcard terminated)
         * <li>3: NoSec mode
         * <li>4: Reserved mode (DTLS mode with multiplexing Security Association support)
         * <li>5-203 : Reserved for future use
         * <li>204-255: Proprietary modes
         * </ul>
         */
        public SmsSecurityMode smsSecurityMode = SmsSecurityMode.NO_SEC;
        /**
         * 6 bytes array stores the KIc, KID, SPI and TAR.
         * <p>
         * The format is defined in Section E.1.2 of the LwM2M version 1.0 specification.
         */
        public byte[] smsBindingKeyParam = new byte[] {};
        /**
         * 16-32-48 bytes array stores the values of the key(s) for the SMS binding. This resource MUST only be changed
         * by a bootstrap-server and MUST NOT be readable by any server.
         */
        public byte[] smsBindingKeySecret = new byte[] {};
        /**
         * MSISDN used by the LwM2M Client to send messages to the LwM2M Server via the SMS binding. The LwM2M Client
         * SHALL silently ignore any SMS originated from unknown MSISDN
         */
        public String serverSmsNumber = "";
        /**
         * This identifier uniquely identifies each LwM2M Server configured for the LwM2M Client.
         * <p>
         * This Resource MUST be set when the Bootstrap-Server Resource has false value.
         * <p>
         * Specific ID:0 and ID:65535 values MUST NOT be used for identifying the LwM2M Server (Section 6.3 of the LwM2M
         * version 1.0 specification).
         */
        public Integer serverId;
        /**
         * Relevant information for a Bootstrap-Server only.
         * <p>
         * The number of seconds to wait before initiating a Client Initiated Bootstrap once the LwM2M Client has
         * determined it should initiate this bootstrap mode.
         * <p>
         * In case client initiated bootstrap is supported by the LwM2M Client, this resource MUST be supported.
         */
        public Integer clientOldOffTime = 1;
        /**
         * The LwM2M Client MUST purge the LwM2M Bootstrap-Server Account after the timeout value given by this
         * resource.
         * <p>
         * The lowest timeout value is 1. If the value is set to 0, or if this resource is not instantiated, the
         * Bootstrap-Server Account lifetime is infinite.
         */
        public Integer bootstrapServerAccountTimeout = 0;

        /**
         * The Certificate Usage Resource specifies the semantic of the certificate or raw public key stored in the
         * Server Public Key Resource, which is used to match the certificate presented in the TLS/DTLS handshake.
         * <ul>
         * <li>0: CA constraint
         * <li>1: service certificate constraint
         * <li>2: trust anchor assertion
         * <li>3: domain-issued certificate (default if missing)
         * </ul>
         */
        public CertificateUsage certificateUsage;

        @Override
        public String toString() {
            // Note : secretKey and smsBindingKeySecret are explicitly excluded from the display for security purposes
            return String.format(
                    "ServerSecurity [uri=%s, bootstrapServer=%s, securityMode=%s, publicKeyOrId=%s, serverPublicKey=%s, smsSecurityMode=%s, smsBindingKeySecret=%s, serverSmsNumber=%s, serverId=%s, clientOldOffTime=%s, bootstrapServerAccountTimeout=%s, certificateUsage=%s]",
                    uri, bootstrapServer, securityMode, Arrays.toString(publicKeyOrId),
                    Arrays.toString(serverPublicKey), smsSecurityMode, Arrays.toString(smsBindingKeyParam),
                    serverSmsNumber, serverId, clientOldOffTime, bootstrapServerAccountTimeout, certificateUsage);
        }
    }

    /**
     * ACL configuration (object 2) as defined in LWM2M 1.0.x TS.
     * <p>
     * Access Control Object is used to check whether the LwM2M Server has access right for performing an operation.
     */
    public static class ACLConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        /** The Object ID of the Object Instance for which ACL are applied. */
        public int objectId;
        /** The Object instance ID of the Object Instance for which ACL are applied. */
        public int objectInstanceId;

        /**
         * The Resource Instance ID MUST be the Short Server ID of a certain LwM2M Server for which associated access
         * rights are contained in the Resource Instance value.
         * <p>
         * The Resource Instance ID 0 is a specific ID, determining the ACL Instance which contains the default access
         * rights. Each bit set in the Resource Instance value, grants an access right to the LwM2M Server to the
         * corresponding operation.
         * <p>
         * The bit order is specified as below.
         * <ul>
         * <li>1st LSB: R(Read, Observe, Discover, Write-Attributes)
         * <li>2nd LSB: W(Write)
         * <li>3rd LSB: E(Execute)
         * <li>4th LSB: D(Delete)
         * <li>5th LSB: C(Create) Other bits are reserved for future use.
         * </ul>
         */
        public Map<Integer, Long> acls;
        /**
         * Short Server ID of a certain LwM2M Server; only such an LwM2M Server can manage the Resources of this Object
         * Instance.
         * <p>
         * The specific value MAX_ID=65535 means this Access Control Object Instance is created and modified during a
         * Bootstrap phase only.
         */
        public Integer AccessControlOwner;

        @Override
        public String toString() {
            return String.format("ACLConfig [objectId=%s, objectInstanceId=%s, ACLs=%s, AccessControlOwner=%s]",
                    objectId, objectInstanceId, acls, AccessControlOwner);
        }
    }

    @Override
    public String toString() {
        return String.format("BootstrapConfig [servers=%s, security=%s, acls=%s]", servers, security, acls);
    }
}
