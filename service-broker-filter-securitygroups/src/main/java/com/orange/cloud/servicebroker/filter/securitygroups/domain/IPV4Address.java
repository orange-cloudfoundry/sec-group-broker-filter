package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some code has been copied from @link org.apache.commons.net.util.SubnetUtils
 */
@Value.Immutable
public abstract class IPV4Address implements Range<IPV4Address> {

    private static final Pattern addressPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    @Value.Parameter
    public abstract String value();

    @Override
    public boolean isInRange(IPV4Address candidate) {
        return Optional.ofNullable(candidate)
                .map(IPV4Address::value)
                .map(value()::equals)
                .orElse(Boolean.FALSE);
    }

    @Value.Check
    protected void validate() {
        Matcher matcher = addressPattern.matcher(value());
        if (!matcher.matches())
            throw new IllegalArgumentException(String.format("Invalid IP address : %s", value()));
    }

    @Value.Lazy
    public boolean greaterOrEqualsTo(IPV4Address candidate) {
        return Optional.ofNullable(candidate)
                .map(IPV4Address::value)
                .map(ip -> {
                    long ipLong = (long) this.toInteger(ip) & 4294967295L;
                    long valueLong = (long) this.toInteger(value()) & 4294967295L;
                    return valueLong >= ipLong;
                })
                .orElse(Boolean.FALSE);
    }

    @Value.Lazy
    public boolean lessOrEqualsTo(IPV4Address candidate) {
        return Optional.ofNullable(candidate)
                .map(IPV4Address::value)
                .map(ip -> {
                    long ipLong = (long) this.toInteger(ip) & 4294967295L;
                    long valueLong = (long) this.toInteger(value()) & 4294967295L;
                    return valueLong <= ipLong;
                })
                .orElse(Boolean.FALSE);
    }

    private int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return this.matchAddress(matcher);
        } else {
            throw new IllegalArgumentException(String.format("Invalid IP address : %s", value()));
        }
    }

    private int matchAddress(Matcher matcher) {
        int addr = 0;

        for (int i = 1; i <= 4; ++i) {
            int n = this.rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255);
            addr |= (n & 255) << 8 * (4 - i);
        }

        return addr;
    }

    private int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end) {
            return value;
        } else {
            throw new IllegalArgumentException("Value [" + value + "] not in range [" + begin + "," + end + "]");
        }
    }

}
