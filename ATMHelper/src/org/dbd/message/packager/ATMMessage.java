package org.dbd.message.packager;

import org.jpos.fsdpackager.AFSDFieldPackager;
import org.jpos.fsdpackager.FSDMsgX;
import org.jpos.fsdpackager.FixedFieldPackager;
import org.jpos.fsdpackager.LookAheadPackager;
import org.jpos.fsdpackager.VariableFieldPackager;
import org.jpos.iso.AsciiInterpreter;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.LiteralInterpreter;

/**
 * @author Chhil
 *
 * This is an incomplete parser, parses upto cassette dispense counts.
 * Should give you an idea on how to parse using FSDMSgX module in
 * https://github.com/jpos/jPOS-EE/tree/master/modules/fsdmsgx
 *
 */
public class ATMMessage {

    public FSDMsgX getPackager() {
        Byte fs = new Byte((byte) 0x1c);
        String strFS = new String(new byte[] { 0x1c });
        new Byte((byte) 0x1d);
        String strGS = new String(new byte[] { 0x1d });
        Byte rs = new Byte((byte) 0x1e);
        String strRS =new String(new byte[] { 0x1e });

        FSDMsgX creq = new FSDMsgX("DBDMessage");
        AFSDFieldPackager messageClass = new FixedFieldPackager(
                Fields.MESSAGE_CLASS, "1", AsciiInterpreter.INSTANCE);
        AFSDFieldPackager messageSubClass = new FixedFieldPackager(
                Fields.MESSAGE_SUB_CLASS, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager fs1 = new FixedFieldPackager(Fields.FIELD_SEPARATOR_1,
                strFS, LiteralInterpreter.INSTANCE);
        AFSDFieldPackager luno = new VariableFieldPackager(Fields.LUNO, 9, fs,
                AsciiInterpreter.INSTANCE);

        // Begin: Optional, present only when MAC is enabled
        AFSDFieldPackager timeVariant = new FixedFieldPackager(
                Fields.TIME_VARIANT, 8, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager fs2 = new FixedFieldPackager(Fields.FIELD_SEPARATOR_2,
                1, LiteralInterpreter.INSTANCE);
        FSDMsgX ifSetTimeVariantPreSep = new FSDMsgX("IFSetTimeVariantPreSep");
        ifSetTimeVariantPreSep.add(fs2);
        ifSetTimeVariantPreSep.add(timeVariant);
        AFSDFieldPackager lookAheadTimevariantfs2 = new LookAheadPackager(
                Fields.TIME_VARIANT_PRE_FS, 0, new Byte((byte) 0x1c),
                ifSetTimeVariantPreSep, null,new String[] {Fields.TIME_VARIANT}, null);
        AFSDFieldPackager fs3 =new FixedFieldPackager(Fields.FIELD_SEPARATOR_3,
                strFS, LiteralInterpreter.INSTANCE);
        AFSDFieldPackager topOfReceipt = new FixedFieldPackager(
                Fields.TOP_OF_RECEIPT, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager messageCoOrdNumber = new FixedFieldPackager(
                Fields.MsgCoOrdNumber, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager fs4 = new FixedFieldPackager(
                Fields.FIELD_SEPARATOR_4, strFS, LiteralInterpreter.INSTANCE);
        AFSDFieldPackager track2 = new VariableFieldPackager(Fields.TRACK2, 40,
                fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager track3 = new VariableFieldPackager(Fields.TRACK3, 106,
                fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager opCodeData = new VariableFieldPackager(Fields.OP_CODE,
                8, fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager amount = new VariableFieldPackager(Fields.Amount, 12,
                fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager pinBlock = new VariableFieldPackager(Fields.PINBLOCK,
                16, fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager bufferB = new VariableFieldPackager(Fields.BUFFER_B,
                32, fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager bufferC = new VariableFieldPackager(Fields.BUFFER_C,
                32, fs, AsciiInterpreter.INSTANCE);

        // BEGIN : Optional Track1
        AFSDFieldPackager track1ID = new FixedFieldPackager(Fields.TRACK1_ID,
                1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager track1 = new VariableFieldPackager(Fields.TRACK1, 78,
                fs, AsciiInterpreter.INSTANCE);
        FSDMsgX ifSetTrack1ID = new FSDMsgX("IFSetTrack1ID");
        ifSetTrack1ID.add(track1ID);
        ifSetTrack1ID.add(track1);

        AFSDFieldPackager lookAheadTrack1ID = new LookAheadPackager(
                "LookAheadTrack1ID", 0, new Byte((byte) 0x31), ifSetTrack1ID,
                null, new String[] {Fields.TRACK1}, null);
        // END: Optional Track1

        // Begin: Optional MICR if IDM is present
        AFSDFieldPackager micrID = new FixedFieldPackager(Fields.MICR_ID, 1,
                AsciiInterpreter.INSTANCE);
        AFSDFieldPackager micr = new VariableFieldPackager(Fields.MICR, 78, fs,
                AsciiInterpreter.INSTANCE);
        FSDMsgX ifSetMicrID = new FSDMsgX("IFSetMicrID");
        ifSetMicrID.add(micrID);
        ifSetMicrID.add(micr);
        AFSDFieldPackager lookAheadMICRID = new LookAheadPackager(
                "LookAheadMICRID", 0, new Byte((byte) 0x32), ifSetMicrID, null,
                new String[] {Fields.MICR}, null);
        // END: Optional TransactionVerification status

        // Begin: if Optional TRAN Verification Data is present
        AFSDFieldPackager tranVeriID = new FixedFieldPackager(Fields.TRAN_VERIFICATION_ID, 1,
                AsciiInterpreter.INSTANCE);
        AFSDFieldPackager serialNumberInLastFuncCommand = new FixedFieldPackager(Fields.SERIAL_NUMBER, 4,AsciiInterpreter.INSTANCE);
        AFSDFieldPackager responseToLastFuncCommand = new FixedFieldPackager(Fields.RESPONSE_TO_LAST_FUNCTION_CMD, 1,AsciiInterpreter.INSTANCE);
        AFSDFieldPackager retractOp = new FixedFieldPackager(Fields.RETRACT_OPERATION, 1,AsciiInterpreter.INSTANCE);
        AFSDFieldPackager gs1 = new FixedFieldPackager("GS1",strGS ,LiteralInterpreter.INSTANCE);
        AFSDFieldPackager deviceDispenser = new FixedFieldPackager(Fields.DEVICE_ID_DISPENSER,"DI01" ,AsciiInterpreter.INSTANCE);
        AFSDFieldPackager rs1 = new FixedFieldPackager("rs1",strRS ,LiteralInterpreter.INSTANCE);

        AFSDFieldPackager denomIDs = new VariableFieldPackager(Fields.CASSETTES_IDS, 5,rs,AsciiInterpreter.INSTANCE);
        AFSDFieldPackager dispensedCounts = new VariableFieldPackager(Fields.DISPENSE_COUNTS,25,fs,AsciiInterpreter.INSTANCE);

        // There can be a fifth cassette and the char after that can be a
        //GS : If coin dispenser is present
        // FS: If coin dispenser absent and Buffer ID and or Rollover counts

        //TODO handle data after Transaction verification status

        FSDMsgX ifSettranVeriID = new FSDMsgX("IFSettranVeriID");
        ifSettranVeriID.add(tranVeriID);
        ifSettranVeriID.add(serialNumberInLastFuncCommand);
        ifSettranVeriID.add(responseToLastFuncCommand);
        ifSettranVeriID.add(retractOp);
        ifSettranVeriID.add(gs1);
        ifSettranVeriID.add(deviceDispenser);
        ifSettranVeriID.add(rs1);

        ifSettranVeriID.add(denomIDs);
        ifSettranVeriID.add(dispensedCounts);

        AFSDFieldPackager lookAheadtranVeriID = new LookAheadPackager(
                "LookAheadtranVeriID", 0, new Byte((byte) 0x36), ifSettranVeriID, null,
                new String[] {Fields.RESPONSE_TO_LAST_FUNCTION_CMD }, null);
        // End: if Optional TRAN Verification Data is present

        creq.add(messageClass);
        creq.add(messageSubClass);
        creq.add(fs1);
        creq.add(luno);
        creq.add(lookAheadTimevariantfs2);
        creq.add(fs3);
        creq.add(topOfReceipt);
        creq.add(messageCoOrdNumber);
        creq.add(fs4);
        creq.add(track2);
        creq.add(track3);
        creq.add(opCodeData);
        creq.add(amount);
        creq.add(pinBlock);
        creq.add(bufferB);
        creq.add(bufferC);
        creq.add(lookAheadTrack1ID);
        creq.add(lookAheadMICRID);
        creq.add(lookAheadtranVeriID);

        return creq;

    }

    public static void main(String[] args) {//throws ISOException {
        ATMMessage m = new ATMMessage();

        FSDMsgX msg = m.getPackager();
        try {
            msg.unpack(ISOUtil.hex2byte("31311C3535353131313132321C1C35353936334345371C31361C3B353838383833383630303030303530363537343D31353130313031313031303030303F1C1C41414220202041431C30303030303030301C343E36313A34383A37353B3A3637363D1C1C1C311C363030303131301D444930311E414243441E30303030303030303031303030303130303030301C3032313030313030343030301C4337383536454535"));
        }
        catch (ISOException ex) {
            // Packager is incomplete , it will throw an exception
            ex.printStackTrace();
        }
        finally {
            System.out.println(msg.getParserTree(""));
            System.out.println(msg.dump(""));

        }
    }

    /*
      <pre>

      <fsdmsgX name="DBDMessage">
    <field id="MessageClass" value="1"/>
    <field id="MessageSubClass" value="1"/>
    <field id="fs1" value=""/>
    <field id="LUNO" value="555111122"/>
    <fsdmsgX name="IFSetTimeVariantPreSep">
        <field id="fs2" value=""/>
        <field id="TimeVariant" value="55963CE7"/>
    </fsdmsgX>
    <field id="fs3" value=""/>
    <field id="TopOfReceipt" value="1"/>
    <field id="CoOrdNumber" value="6"/>
    <field id="fs4" value=""/>
    <field id="TRACK2" value=";5888838600000506574=15101011010000?"/>
    <field id="TRACK3" value=""/>
    <field id="OPCode" value="AAB   AC"/>
    <field id="Amount" value="00000000"/>
    <field id="PinBlock" value="4>61:48:75;:676="/>
    <field id="BufferB" value=""/>
    <field id="BufferC" value=""/>
    <fsdmsgX name="IFSetTrack1ID">
        <field id="TRACK1_ID" value="1"/>
        <field id="TRACK1" value=""/>
    </fsdmsgX>
    <fsdmsgX name="IFSettranVeriID">
        <field id="TRAN_VERIFICATION_ID" value="6"/>
        <field id="SERIAL_NUMBER" value="0001"/>
        <field id="RESPONSE_TO_LAST_FUNCTION_CMD" value="1"/>
        <field id="RETRACT_OPERATION" value="0"/>
        <field id="GS1" value=""/>
        <field id="DEVICE_ID_DISPENSER" value="DI01"/>
        <field id="rs1" value=""/>
        <field id="CASSETTES_IDS" value="ABCD"/>
        <field id="DISPENSE_COUNTS" value="00000000010000100000"/>
    </fsdmsgX>
</fsdmsgX>


ParserTree Output
[DBDMessage]
Field [MessageClass] : Fixed [1] : 1
Field [MessageSubClass] : Fixed [1] : 1
Field [fs1] : Fixed [1] :
Field [LUNO] : VAR[0..9] delimiter[0x1C] or EOM  : 555111122
Field [TimeVariantPreFS] : [LookAhead]
            offset[0] find[0x1C]
                [if found]
                    [IFSetTimeVariantPreSep]
                    Field [fs2] : Fixed [1] :
                    Field [TimeVariant] : Fixed [8] : 55963CE7
                [if not found]
                    [Not Set]
            Check Field[TimeVariant,]
Field [fs3] : Fixed [1] :
Field [TopOfReceipt] : Fixed [1] : 1
Field [CoOrdNumber] : Fixed [1] : 6
Field [fs4] : Fixed [1] :
Field [TRACK2] : VAR[0..40] delimiter[0x1C] or EOM  : ;5888838600000506574=15101011010000?
Field [TRACK3] : VAR[0..106] delimiter[0x1C] or EOM  :
Field [OPCode] : VAR[0..8] delimiter[0x1C] or EOM  : AAB   AC
Field [Amount] : VAR[0..12] delimiter[0x1C] or EOM  : 00000000
Field [PinBlock] : VAR[0..16] delimiter[0x1C] or EOM  : 4>61:48:75;:676=
Field [BufferB] : VAR[0..32] delimiter[0x1C] or EOM  :
Field [BufferC] : VAR[0..32] delimiter[0x1C] or EOM  :
Field [LookAheadTrack1ID] : [LookAhead]
            offset[0] find[0x31]
                [if found]
                    [IFSetTrack1ID]
                    Field [TRACK1_ID] : Fixed [1] : 1
                    Field [TRACK1] : VAR[0..78] delimiter[0x1C] or EOM  :
                [if not found]
                    [Not Set]
            Check Field[TRACK1,]
Field [LookAheadMICRID] : [LookAhead]
            offset[0] find[0x32]
                [if found]
                    [IFSetMicrID]
                    Field [MICR_ID] : Fixed [1]
                    Field [MICR] : VAR[0..78] delimiter[0x1C] or EOM
                [if not found]
                    [Not Set]
            Check Field[MICR,]
Field [LookAheadtranVeriID] : [LookAhead]
            offset[0] find[0x36]
                [if found]
                    [IFSettranVeriID]
                    Field [TRAN_VERIFICATION_ID] : Fixed [1] : 6
                    Field [SERIAL_NUMBER] : Fixed [4] : 0001
                    Field [RESPONSE_TO_LAST_FUNCTION_CMD] : Fixed [1] : 1
                    Field [RETRACT_OPERATION] : Fixed [1] : 0
                    Field [GS1] : Fixed [1] :
                    Field [DEVICE_ID_DISPENSER] : Fixed [4] : DI01
                    Field [rs1] : Fixed [1] :
                    Field [CASSETTES_IDS] : VAR[0..5] delimiter[0x1E] or EOM  : ABCD
                    Field [DISPENSE_COUNTS] : VAR[0..25] delimiter[0x1C] or EOM  : 00000000010000100000
                [if not found]
                    [Not Set]
            Check Field[RESPONSE_TO_LAST_FUNCTION_CMD,]



     */
}
