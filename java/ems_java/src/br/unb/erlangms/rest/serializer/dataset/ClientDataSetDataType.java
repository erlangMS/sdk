package br.unb.erlangms.rest.serializer.dataset;

import br.unb.erlangms.rest.schema.RestFieldType;
import br.unb.erlangms.rest.serializer.dataset.exception.ClientDataSetDataTypeNotFound;

/**
 *
 * @author Jáder Adiél Schmitt
 *
 * Enum que representa od datatypes existentes em um TClientDataSet no delphi
 */
public enum ClientDataSetDataType {
    UNKNOWN("ftUnknown", null, null, 0),
    STRING("ftString", "TStringField", "string", 20),
    SMALLINT("ftSmallint", "TSmallintField", "i2", 0),
    INTEGER("ftInteger", "TIntegerField", "i4", 0),
    WORD("ftWord", "TWordField", "ui2", 0),
    BOOLEAN("ftBoolean", "TBooleanField", "boolean", 0),
    FLOAT("ftFloat", "TFloatField", "r8", 0),
    CURRENCY("ftCurrency", "TCurrencyField", "r8", 0),
    BCD("ftBCD", "TBCDField", "fixed", 4),
    DATE("ftDate", "TDateField", "date", 0),
    TIME("ftTime", "TTimeField", "time", 0),
    DATETIME("ftDateTime", "TDateTimeField", "datetime", 0),
    BYTES("ftBytes", "TBytesField", "bin.hex", 16),
    VARBYTES("ftVarBytes", "TVarBytesField", "bin.hex", 16),
    AUTOINC("ftAutoInc", "TAutoIncField", "i4", 0),
    BLOB("ftBlob", "TBlobField", "bin.hex", 0),
    MEMO("ftMemo", "TMemoField", "bin.hex", 0),
    GRAPHIC("ftGraphic", "TGraphicField", "bin.hex", 0),
    FMTMEMO("ftFmtMemo", "TBlobField", "bin.hex", 0),
    PARADOXOLE("ftParadoxOle", "TBlobField", "bin.hex", 0),
    DBASEOLE("ftDBaseOle", "TBlobField", "bin.hex", 0),
    TYPEDBINARY("ftTypedBinary", "TBlobField", "bin.hex", 0),
    CURSOR("ftCursor", null, "cursor", 0),
    FIXEDCHAR("ftFixedChar", "TStringField", "string", 20),
    WIDESTRING("ftWideString", "TWideStringField", "string.uni", 0),
    LARGEINT("ftLargeint", "TLargeIntField", "i8", 0),
    ADT("ftADT", "TADTField", null, 0),
    ARRAY("ftArray", "TArrayField", "array", 10),
    REFERENCE("ftReference", "TReferenceField", "reference", 0),
    DATASET("ftDataSet", "TDataSetField", "dataset", 0),
    ORABLOB("ftOraBlob", "TBlobField", "bin.hex", 0),
    ORACLOB("ftOraClob", "TMemoField", "bin.hex", 0),
    VARIANT("ftVariant", "TVariantField", "variant", 0),
    INTERFACE("ftInterface", "TInterfaceField", "interface", 0),
    DISPATCH("ftIDispatch", "TIDispatchField", "idispatch", 0),
    GUID("ftGuid", "TGuidField", "string", 0),
    TIMESTAMP("ftTimeStamp", "TSQLTimeStampField", "SQLdateTime", 0),
    FMTBCD("ftFMTBcd", "TFMTBcdField", "fixedFMT", 0);

    private final String name;
    private final String className;
    private final String xmlType;
    private final Integer defautSize;

    ClientDataSetDataType(final String name, final String className, final String xmlType, final Integer defaultSize){
        this.name = name;
        this.className = className;
        this.xmlType = xmlType;
        this.defautSize = defaultSize;
    }
    public String getName() {
        return name;
    }
    public String getClassName() {
        return className;
    }
    public String getXmlType() {
        return xmlType;
    }
    public Integer getDefautSize() {
        return defautSize;
    }

    public static ClientDataSetDataType fromRestApiType(final RestFieldType restFieldType){
        switch(restFieldType){
            case STRING:
                return ClientDataSetDataType.STRING;
            case INTEGER:
                return ClientDataSetDataType.INTEGER;
            case DOUBLE:
                return ClientDataSetDataType.FLOAT;
            case LONG:
                return ClientDataSetDataType.LARGEINT;
            case DATE:
                return ClientDataSetDataType.DATE;
            case TIME:
                return ClientDataSetDataType.TIME;
            case BOOLEAN:
                return ClientDataSetDataType.BOOLEAN;
            default:
                throw new ClientDataSetDataTypeNotFound(restFieldType);
        }
    }

}
