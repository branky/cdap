/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.continuuity.metadata.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a workflow
 */
public class Workflow implements org.apache.thrift.TBase<Workflow, Workflow._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Workflow");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField APPLICATION_FIELD_DESC = new org.apache.thrift.protocol.TField("application", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField EXISTS_FIELD_DESC = new org.apache.thrift.protocol.TField("exists", org.apache.thrift.protocol.TType.BOOL, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new WorkflowStandardSchemeFactory());
    schemes.put(TupleScheme.class, new WorkflowTupleSchemeFactory());
  }

  private String id; // required
  private String application; // required
  private String name; // optional
  private boolean exists; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    APPLICATION((short)2, "application"),
    NAME((short)3, "name"),
    EXISTS((short)4, "exists");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ID
          return ID;
        case 2: // APPLICATION
          return APPLICATION;
        case 3: // NAME
          return NAME;
        case 4: // EXISTS
          return EXISTS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __EXISTS_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  private _Fields optionals[] = {_Fields.NAME,_Fields.EXISTS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.APPLICATION, new org.apache.thrift.meta_data.FieldMetaData("application", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.EXISTS, new org.apache.thrift.meta_data.FieldMetaData("exists", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Workflow.class, metaDataMap);
  }

  public Workflow() {
    this.exists = true;

  }

  public Workflow(
    String id,
    String application)
  {
    this();
    this.id = id;
    this.application = application;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Workflow(Workflow other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetId()) {
      this.id = other.id;
    }
    if (other.isSetApplication()) {
      this.application = other.application;
    }
    if (other.isSetName()) {
      this.name = other.name;
    }
    this.exists = other.exists;
  }

  public Workflow deepCopy() {
    return new Workflow(this);
  }

  @Override
  public void clear() {
    this.id = null;
    this.application = null;
    this.name = null;
    this.exists = true;

  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void unsetId() {
    this.id = null;
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return this.id != null;
  }

  public void setIdIsSet(boolean value) {
    if (!value) {
      this.id = null;
    }
  }

  public String getApplication() {
    return this.application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public void unsetApplication() {
    this.application = null;
  }

  /** Returns true if field application is set (has been assigned a value) and false otherwise */
  public boolean isSetApplication() {
    return this.application != null;
  }

  public void setApplicationIsSet(boolean value) {
    if (!value) {
      this.application = null;
    }
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void unsetName() {
    this.name = null;
  }

  /** Returns true if field name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }

  public boolean isExists() {
    return this.exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
    setExistsIsSet(true);
  }

  public void unsetExists() {
    __isset_bit_vector.clear(__EXISTS_ISSET_ID);
  }

  /** Returns true if field exists is set (has been assigned a value) and false otherwise */
  public boolean isSetExists() {
    return __isset_bit_vector.get(__EXISTS_ISSET_ID);
  }

  public void setExistsIsSet(boolean value) {
    __isset_bit_vector.set(__EXISTS_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((String)value);
      }
      break;

    case APPLICATION:
      if (value == null) {
        unsetApplication();
      } else {
        setApplication((String)value);
      }
      break;

    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((String)value);
      }
      break;

    case EXISTS:
      if (value == null) {
        unsetExists();
      } else {
        setExists((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case APPLICATION:
      return getApplication();

    case NAME:
      return getName();

    case EXISTS:
      return Boolean.valueOf(isExists());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case APPLICATION:
      return isSetApplication();
    case NAME:
      return isSetName();
    case EXISTS:
      return isSetExists();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Workflow)
      return this.equals((Workflow)that);
    return false;
  }

  public boolean equals(Workflow that) {
    if (that == null)
      return false;

    boolean this_present_id = true && this.isSetId();
    boolean that_present_id = true && that.isSetId();
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (!this.id.equals(that.id))
        return false;
    }

    boolean this_present_application = true && this.isSetApplication();
    boolean that_present_application = true && that.isSetApplication();
    if (this_present_application || that_present_application) {
      if (!(this_present_application && that_present_application))
        return false;
      if (!this.application.equals(that.application))
        return false;
    }

    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }

    boolean this_present_exists = true && this.isSetExists();
    boolean that_present_exists = true && that.isSetExists();
    if (this_present_exists || that_present_exists) {
      if (!(this_present_exists && that_present_exists))
        return false;
      if (this.exists != that.exists)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_id = true && (isSetId());
    builder.append(present_id);
    if (present_id)
      builder.append(id);

    boolean present_application = true && (isSetApplication());
    builder.append(present_application);
    if (present_application)
      builder.append(application);

    boolean present_name = true && (isSetName());
    builder.append(present_name);
    if (present_name)
      builder.append(name);

    boolean present_exists = true && (isSetExists());
    builder.append(present_exists);
    if (present_exists)
      builder.append(exists);

    return builder.toHashCode();
  }

  public int compareTo(Workflow other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Workflow typedOther = (Workflow)other;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, typedOther.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetApplication()).compareTo(typedOther.isSetApplication());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetApplication()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.application, typedOther.application);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetName()).compareTo(typedOther.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, typedOther.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExists()).compareTo(typedOther.isSetExists());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExists()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.exists, typedOther.exists);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Workflow(");
    boolean first = true;

    sb.append("id:");
    if (this.id == null) {
      sb.append("null");
    } else {
      sb.append(this.id);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("application:");
    if (this.application == null) {
      sb.append("null");
    } else {
      sb.append(this.application);
    }
    first = false;
    if (isSetName()) {
      if (!first) sb.append(", ");
      sb.append("name:");
      if (this.name == null) {
        sb.append("null");
      } else {
        sb.append(this.name);
      }
      first = false;
    }
    if (isSetExists()) {
      if (!first) sb.append(", ");
      sb.append("exists:");
      sb.append(this.exists);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (!isSetId()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' is unset! Struct:" + toString());
    }

    if (!isSetApplication()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'application' is unset! Struct:" + toString());
    }

  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class WorkflowStandardSchemeFactory implements SchemeFactory {
    public WorkflowStandardScheme getScheme() {
      return new WorkflowStandardScheme();
    }
  }

  private static class WorkflowStandardScheme extends StandardScheme<Workflow> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Workflow struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.id = iprot.readString();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // APPLICATION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.application = iprot.readString();
              struct.setApplicationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.name = iprot.readString();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // EXISTS
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.exists = iprot.readBool();
              struct.setExistsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, Workflow struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.id != null) {
        oprot.writeFieldBegin(ID_FIELD_DESC);
        oprot.writeString(struct.id);
        oprot.writeFieldEnd();
      }
      if (struct.application != null) {
        oprot.writeFieldBegin(APPLICATION_FIELD_DESC);
        oprot.writeString(struct.application);
        oprot.writeFieldEnd();
      }
      if (struct.name != null) {
        if (struct.isSetName()) {
          oprot.writeFieldBegin(NAME_FIELD_DESC);
          oprot.writeString(struct.name);
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetExists()) {
        oprot.writeFieldBegin(EXISTS_FIELD_DESC);
        oprot.writeBool(struct.exists);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class WorkflowTupleSchemeFactory implements SchemeFactory {
    public WorkflowTupleScheme getScheme() {
      return new WorkflowTupleScheme();
    }
  }

  private static class WorkflowTupleScheme extends TupleScheme<Workflow> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Workflow struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.id);
      oprot.writeString(struct.application);
      BitSet optionals = new BitSet();
      if (struct.isSetName()) {
        optionals.set(0);
      }
      if (struct.isSetExists()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetName()) {
        oprot.writeString(struct.name);
      }
      if (struct.isSetExists()) {
        oprot.writeBool(struct.exists);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Workflow struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.id = iprot.readString();
      struct.setIdIsSet(true);
      struct.application = iprot.readString();
      struct.setApplicationIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.name = iprot.readString();
        struct.setNameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.exists = iprot.readBool();
        struct.setExistsIsSet(true);
      }
    }
  }

}

