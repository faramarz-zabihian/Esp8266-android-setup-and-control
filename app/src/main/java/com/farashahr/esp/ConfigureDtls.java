/*******************************************************************************
 * Copyright (c) 2018 Vikram and others.
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
 *    Vikram - Initial creation
 *    Achim Kraus (Bosch Software Innovations GmbH) - introduce configurable
 *                                                    key store type and
 *                                                    InputStreamFactory.
 ******************************************************************************/

package com.farashahr.esp;

import org.eclipse.californium.elements.util.SslContextUtil;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedSinglePskStore;
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider;
import org.eclipse.californium.scandium.dtls.x509.StaticNewAdvancedCertificateVerifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.concurrent.TimeUnit;

/**
 * class ConfigureDTLS.<br>
 * This class is used to configure dtls connector for both client and server side connections.
 * It allows to configure dtls connector in three different modes.
 * The class variables PSK, CERTIFICATE_MODE, and RPK_MODE define three different modes of dtls connector.
 * <br>PSK_MODE: supports PSK mode if this variable is set to true and to false if not.
 * <br>CERTIFICATE_MODE: supports certificate based authentication, if this variable is
 * set to true and false if not.
 * <br>RPK_MODE: alternatively supports RPK mode if this variable is set to true and to false if not.
 * <br>An endpoint may either support all the three modes or must support atleast one mode.
 */
public class ConfigureDtls {

    private static final boolean PSK_MODE = true;
    public static final String PSK_IDENTITY = "far";
    public static final byte[] PSK_SECRET = "0123456789123457".getBytes();

    public static void loadCredentials(DtlsConnectorConfig.Builder dtlsConfig) {

    }
}