/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @version 2018/10/04 21:48:41
 */
public class BuiltinBean {

    private SchoolEnum schoolEnum;

    private Date date;

    private File file;

    private Class someClass;

    private BigInteger bigInteger;

    private BigDecimal bigDecimal;

    private StringBuilder stringBuilder;

    private StringBuffer stringBuffer;

    /**
     * Get the bigDecimal property of this {@link BuiltinBean}.
     * 
     * @return The bigDecimal prperty.
     */
    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    /**
     * Set the bigDecimal property of this {@link BuiltinBean}.
     * 
     * @param bigDecimal The bigDecimal value to set.
     */
    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    /**
     * Get the bigInteger property of this {@link BuiltinBean}.
     * 
     * @return The bigInteger prperty.
     */
    public BigInteger getBigInteger() {
        return bigInteger;
    }

    /**
     * Set the bigInteger property of this {@link BuiltinBean}.
     * 
     * @param bigInteger The bigInteger value to set.
     */
    public void setBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    /**
     * Get the file property of this {@link BuiltinBean}.
     * 
     * @return The file prperty.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file property of this {@link BuiltinBean}.
     * 
     * @param file The file value to set.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Get the schoolEnum property of this {@link BuiltinBean}.
     * 
     * @return The schoolEnum prperty.
     */
    public SchoolEnum getSchoolEnum() {
        return schoolEnum;
    }

    /**
     * Set the schoolEnum property of this {@link BuiltinBean}.
     * 
     * @param schoolEnum The schoolEnum value to set.
     */
    public void setSchoolEnum(SchoolEnum schoolEnum) {
        this.schoolEnum = schoolEnum;
    }

    /**
     * Get the someClass property of this {@link BuiltinBean}.
     * 
     * @return The someClass prperty.
     */
    public Class getSomeClass() {
        return someClass;
    }

    /**
     * Set the someClass property of this {@link BuiltinBean}.
     * 
     * @param someClass The someClass value to set.
     */
    public void setSomeClass(Class someClass) {
        this.someClass = someClass;
    }

    /**
     * Get the stringBuffer property of this {@link BuiltinBean}.
     * 
     * @return The stringBuffer prperty.
     */
    public StringBuffer getStringBuffer() {
        return stringBuffer;
    }

    /**
     * Set the stringBuffer property of this {@link BuiltinBean}.
     * 
     * @param stringBuffer The stringBuffer value to set.
     */
    public void setStringBuffer(StringBuffer stringBuffer) {
        this.stringBuffer = stringBuffer;
    }

    /**
     * Get the stringBuilder property of this {@link BuiltinBean}.
     * 
     * @return The stringBuilder prperty.
     */
    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    /**
     * Set the stringBuilder property of this {@link BuiltinBean}.
     * 
     * @param stringBuilder The stringBuilder value to set.
     */
    public void setStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

}
