package com.github.dactiv.healthan.spring.web.mvc;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.exception.SystemException;
import com.github.dactiv.healthan.spring.web.device.DeviceUtils;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * spring mvc 工具类
 *
 * @author maurice.chen
 **/
public class SpringMvcUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcUtils.class);

    private final static String UNKNOWN_STRING = "unknown";

    private final static Integer IP_MIN_LENGTH = 15;

    public final static String DEFAULT_ATTACHMENT_NAME = "attachment;filename=";

    public final static String ANT_PATH_MATCH_ALL = "/**";

    public final static String HTTP_PROTOCOL_PREFIX = "http://";

    public final static String HTTPS_PROTOCOL_PREFIX = "https://";

    /**
     * 获取 request 的 attribute
     *
     * @param name attribute 名称
     * @param <T>  attribute 类型
     *
     * @return attribute 值
     */
    public static <T> T getRequestAttribute(String name) {
        return Casts.cast(getCurrentRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_REQUEST));
    }

    public static void removeRequestAttribute(String name) {
        getCurrentRequestAttributes().removeAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 设置 session 的 attribute
     *
     * @param name  attribute 名称
     * @param value 值
     */
    public static void setSessionAttribute(String name, Object value) {
        getCurrentRequestAttributes().setAttribute(name, value, RequestAttributes.SCOPE_SESSION);
    }

    /**
     * 设置 request 的 attribute
     *
     * @param name  attribute 名称
     * @param value 值
     */
    public static void setRequestAttribute(String name, Object value) {
        getCurrentRequestAttributes().setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 获取 session 的 attribute
     *
     * @param name attribute 名称
     * @param <T>  attribute 类型
     *
     * @return attribute 值
     */
    public static <T> T getSessionAttribute(String name) {
        return Casts.cast(getCurrentRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_SESSION));
    }

    public static void removeSessionAttribute(String name) {
        getCurrentRequestAttributes().removeAttribute(name, RequestAttributes.SCOPE_SESSION);
    }

    /**
     * 获取 spring mvc RequestAttributes
     *
     * @return RequestAttributes
     */
    public static RequestAttributes getCurrentRequestAttributes() {
        return RequestContextHolder.currentRequestAttributes();
    }

    /**
     * 获取 HttpServletRequest
     *
     * @return httpServletRequest
     */
    public static Optional<HttpServletRequest> getHttpServletRequest() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes) {
            return Optional.of(Casts.cast(requestAttributes, ServletRequestAttributes.class).getRequest());
        }

        return Optional.empty();
    }

    /**
     * 获取 HttpServletResponse
     *
     * @return httpServletResponse
     */
    public static Optional<HttpServletResponse> getHttpServletResponse() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes) {
            return Optional.ofNullable(Casts.cast(requestAttributes, ServletRequestAttributes.class).getResponse());
        }

        return Optional.empty();
    }

    /**
     * 获取 http 响应状态
     *
     * @param httpServletResponse http servlet response
     *
     * @return http 响应状态
     */
    public static HttpStatus getHttpStatus(HttpServletResponse httpServletResponse) {
        HttpStatus result;
        try {
            result = HttpStatus.valueOf(httpServletResponse.getStatus());
        } catch (Exception e) {
            result = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return result;
    }

    /**
     * 获取当前设备
     *
     * @return 设备
     */
    public static UserAgent getCurrentDevice() {
        return DeviceUtils.getCurrentDevice(RequestContextHolder.currentRequestAttributes());
    }

    /**
     * 获取当前设备,如果后去不了，抛出异常
     *
     * @return 设备
     */
    public static UserAgent getRequiredCurrentDevice() {

        Optional<HttpServletRequest> optional = getHttpServletRequest();

        if (!optional.isPresent()) {
            throw new SystemException("当前线程中无法获取 HttpServletRequest 信息");
        }
        return DeviceUtils.getRequiredCurrentDevice(optional.get());
    }

    /**
     * 获取设备唯一识别
     *
     * @return 设备唯一识别
     */
    public static String getDeviceIdentified() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return getDeviceIdentified(optional.orElseThrow(() -> new SystemException("当前线程中无法获取 HttpServletRequest 信息")));
    }

    /**
     * 获取设备唯一识别
     *
     * @param request http servlet request
     *
     * @return 设备唯一识别
     */
    public static String getDeviceIdentified(HttpServletRequest request) {

        String deviceIdentified = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isBlank(deviceIdentified)) {
            deviceIdentified = request.getParameter(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
        }

        if (StringUtils.isBlank(deviceIdentified)) {
            deviceIdentified = request.getSession().getId();
        }

        return deviceIdentified;
    }

    /**
     * 获取请求的头里的设备唯一识别
     *
     * @return 唯一识别
     */
    public static String getRequestHeaderDeviceIdentified() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();

        return optional
                .map(request -> request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME))
                .orElse(null);

    }

    /**
     * 通过 rest 结果集构造下载类型的 ResponseEntity
     *
     * @param result rest 结果集
     *
     * @return 下载类型的 ResponseEntity
     */
    public static ResponseEntity<byte[]> createDownloadResponseEntity(RestResult<byte[]> result) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(SpringMvcUtils.DEFAULT_ATTACHMENT_NAME, URLEncoder.encode(result.getMessage(), "UTF-8"));
        return new ResponseEntity<>(result.getData(), headers, HttpStatus.OK);
    }

    /**
     * 创建下载类型的 ResponseEntity
     *
     * @param filename 下载文件名称
     * @param path     文件路径
     *
     * @return 下载类型的 ResponseEntity
     *
     * @throws IOException 获取路径文件失败抛出
     */
    public static ResponseEntity<byte[]> createDownloadResponseEntity(String filename, String path) throws IOException {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(DEFAULT_ATTACHMENT_NAME, URLEncoder.encode(filename, "UTF-8"));

        return new ResponseEntity<>(FileCopyUtils.copyToByteArray(new File(path)), headers, HttpStatus.CREATED);
    }

    /**
     * 获取 ip 地址
     *
     * @return ip 地址
     */
    public static String getIpAddress() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return optional.map(SpringMvcUtils::getIpAddress).orElse(UNKNOWN_STRING);
    }

    /**
     * 获取 ip 地址
     *
     * @param request request http servlet reques
     *
     * @return ip 地址
     */
    public static String getIpAddress(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        //使用代理，则获取第一个IP地址
        if (StringUtils.isNotBlank(ip) && ip.length() > IP_MIN_LENGTH) {
            if (ip.indexOf(Casts.COMMA) > 0) {
                ip = ip.substring(0, ip.indexOf(Casts.COMMA));
            }
        }

        return ip;
    }

    /**
     * 获取 mac 地址
     *
     * @return  mac 地址
     */
    public static String getMacAddress() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return optional.map(SpringMvcUtils::getMacAddress).orElse(UNKNOWN_STRING);
    }

    /**
     * 获取 mac 地址
     *
     * @param request  mac 地址
     *
     * @return  mac 地址
     */
    public static String getMacAddress(HttpServletRequest request) {

        try {
            InetAddress ipAddress = InetAddress.getByName(request.getRemoteAddr());
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ipAddress);
            byte[] macAddressBytes = networkInterface.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < macAddressBytes.length; i++) {
                sb.append(String.format("%02X%s", macAddressBytes[i], (i < macAddressBytes.length - 1) ? Casts.NEGATIVE : StringUtils.EMPTY));
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.warn("获取 mac 地址出错", e);
            return UNKNOWN_STRING;
        }

    }

    /**
     * 判断是否回路地址
     *
     * @param host host 值
     *
     * @return true 是，否则 false
     */
    public static boolean isLoopbackAddress(String host) {
        if (!org.springframework.util.StringUtils.hasText(host)) {
            return false;
        }
        // IPv6 loopback address should either be "0:0:0:0:0:0:0:1" or "::1"
        if ("[0:0:0:0:0:0:0:1]".equals(host) || "[::1]".equals(host)) {
            return true;
        }
        // IPv4 loopback address ranges from 127.0.0.1 to 127.255.255.255
        String[] ipv4Octets = host.split("\\.");
        if (ipv4Octets.length != 4) {
            return false;
        }
        try {
            int[] address = new int[ipv4Octets.length];
            for (int i=0; i < ipv4Octets.length; i++) {
                address[i] = Integer.parseInt(ipv4Octets[i]);
            }
            return address[0] == 127 && address[1] >= 0 && address[1] <= 255 && address[2] >= 0 &&
                    address[2] <= 255 && address[3] >= 1 && address[3] <= 255;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

}
