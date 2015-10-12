package com.github.therapi.jackson.enums;

import org.junit.Test;

import static com.github.therapi.jackson.enums.CaseFormatHelper.toLowerCamel;
import static org.junit.Assert.assertEquals;

public class CaseFormatHelperTest {
    @Test
    public void testConversionToLowerCamel() {
        assertEquals("a", toLowerCamel("A"));
        assertEquals("a", toLowerCamel("a"));
        assertEquals("à", toLowerCamel("à"));
        assertEquals("à", toLowerCamel("À"));

        assertEquals("xmlHttpRequest", toLowerCamel("xmlHTTPRequest"));
        assertEquals("xmlHttpRequest", toLowerCamel("XMLHttpRequest"));
        assertEquals("xmlHttpRequest", toLowerCamel("XMLHttpREQUEST"));
        assertEquals("xmlhttprequest", toLowerCamel("XMLHTTPREQUEST"));
        assertEquals("xmlHttpRequèst", toLowerCamel("XMLHttpREQUÈST"));

        assertEquals("xmlHttpRequest", toLowerCamel("XML_HTTP_REQUEST"));
        assertEquals("xmlHttpRequest", toLowerCamel("xml_http_request"));
        assertEquals("xmlHttpRequest", toLowerCamel("xml_HttpRequest"));
        assertEquals("xmlHttpRequest", toLowerCamel("xml_HTTPRequest"));
        assertEquals("xmlHttpRequest", toLowerCamel("xmlHTTP_Request"));

        assertEquals("version123", toLowerCamel("VERSION_123"));
        assertEquals("version123", toLowerCamel("Version123"));

        assertEquals("version1_2_3", toLowerCamel("VERSION_1_2_3"));
        assertEquals("version1_2_3", toLowerCamel("version_1_2_3"));
        assertEquals("version1_2_3Rc1", toLowerCamel("version_1_2_3_rc1"));
        assertEquals("version1_2_3Rc1", toLowerCamel("version_1_2_3Rc1"));
        assertEquals("version1_2_3rc1", toLowerCamel("version_1_2_3rc1"));
    }
}