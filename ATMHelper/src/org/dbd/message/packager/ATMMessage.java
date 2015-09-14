package org.dbd.message.packager;

import java.util.HashMap;
import java.util.Map;

import org.jpos.fsdpackager.AFSDFieldPackager;
import org.jpos.fsdpackager.BranchFieldPackager;
import org.jpos.fsdpackager.FSDMsgX;
import org.jpos.fsdpackager.FixedFieldPackager;
import org.jpos.fsdpackager.LookAheadPackager;
import org.jpos.fsdpackager.OptionalPackager;
import org.jpos.fsdpackager.VariableFieldPackager;
import org.jpos.iso.AsciiInterpreter;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.LiteralInterpreter;

/**
 * @author Chhil
 *
 *         This is an incomplete parser, parses upto cassette dispense counts.
 *         Should give you an idea on how to parse using FSDMSgX module in
 *         https://github.com/jpos/jPOS-EE/tree/master/modules/fsdmsgx
 *
 */
public class ATMMessage {

    Byte   fs    = new Byte((byte) 0x1c);
    String strFS = new String(new byte[] { 0x1c });
    Byte   gs    = new Byte((byte) 0x1d);
    String strGS = new String(new byte[] { 0x1d });
    Byte   rs    = new Byte((byte) 0x1e);
    String strRS = new String(new byte[] { 0x1e });

    public FSDMsgX getMessagePackager() {
        FSDMsgX msg = new FSDMsgX("MSG");
        AFSDFieldPackager messageClass = new FixedFieldPackager(
                Fields.MESSAGE_CLASS, 1, AsciiInterpreter.INSTANCE);
        msg.add(messageClass);

        Map<String, AFSDFieldPackager> cases = new HashMap<String, AFSDFieldPackager>();
        cases.put("1", getUnsolicitedPackager());
        cases.put("2", getSolicitedPackager());
        BranchFieldPackager branch1 = new BranchFieldPackager(
                "MessageClassBranch", Fields.MESSAGE_CLASS, cases, null);
        msg.add(branch1);

        return msg;

    }

    private AFSDFieldPackager getSolicitedPackager() {
        FSDMsgX container = new FSDMsgX("SolicitedStatus");
        AFSDFieldPackager messageSubClass = new FixedFieldPackager(
                Fields.MESSAGE_SUB_CLASS, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager fs5 = new FixedFieldPackager(Fields.FIELD_SEPARATOR_5,
                strFS, LiteralInterpreter.INSTANCE);
        AFSDFieldPackager luno = new VariableFieldPackager(Fields.LUNO, 9, fs,
                AsciiInterpreter.INSTANCE);
        AFSDFieldPackager messageSeqNumber = new VariableFieldPackager(
                Fields.MESSAGE_SEQUENCE_NUMBER, 8, fs,
                AsciiInterpreter.INSTANCE);
        AFSDFieldPackager statusDescriptor = new FixedFieldPackager(
                Fields.STATUS_DESCRIPTOR, 1, AsciiInterpreter.INSTANCE);

        Map cases = new HashMap<String, AFSDFieldPackager>();
        cases.put("8", getDeviceFaultPackager());

        AFSDFieldPackager branchStatusDescriptor = new BranchFieldPackager(
                "BranchstatusDescriptor", Fields.STATUS_DESCRIPTOR, cases,
                getDefaultForStatusDescriptorPackager());

        container.add(messageSubClass);
        container.add(fs5);
        container.add(luno);
        container.add(messageSeqNumber);
        container.add(statusDescriptor);
        container.add(branchStatusDescriptor);
        return container;
    }

    private AFSDFieldPackager getDefaultForStatusDescriptorPackager() {

        FSDMsgX container = new FSDMsgX("DefaultStatusDescriptor");
        FSDMsgX useIfSet = new FSDMsgX("UseIfCoinSet");
        AFSDFieldPackager fs7 = new VariableFieldPackager(
                Fields.FIELD_SEPARATOR_7, 0, fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager coinsDispensed = new VariableFieldPackager(
                Fields.COINS_DISPENSED, 3, fs, AsciiInterpreter.INSTANCE);

        useIfSet.add(coinsDispensed);
        AFSDFieldPackager lookAheadCoinsDispensed = new LookAheadPackager(
                "LookForCoins", 5, fs, useIfSet, null,
                new String[] { Fields.COINS_DISPENSED }, null);
        AFSDFieldPackager useIfSet2 = new VariableFieldPackager(
                Fields.MDS_STATUS, fs, AsciiInterpreter.INSTANCE);

        AFSDFieldPackager lookAheadMDSStatus = new LookAheadPackager(
                "LookForMDSStatusColon", 5, new Byte((byte) ':'), useIfSet2,
                null, new String[] { Fields.MDS_STATUS }, null);

        AFSDFieldPackager useIfSet3 = new VariableFieldPackager(
                Fields.BUFFERS_FOLLOWING, fs, AsciiInterpreter.INSTANCE);

        AFSDFieldPackager lookAheadBufferstoFollow = new LookAheadPackager(
                "LookForBufferToFollow", 0, new Byte((byte) 0x39), useIfSet3,
                null, new String[] { Fields.BUFFERS_FOLLOWING }, null);

        AFSDFieldPackager optionalRolloverCounts = new OptionalPackager(
                "OptionalRolloverCounts", new FixedFieldPackager(
                        Fields.ROLLOVER_COUNTS, 12, AsciiInterpreter.INSTANCE));

        container.add(fs7);
        container.add(lookAheadCoinsDispensed);
        container.add(lookAheadMDSStatus);
        container.add(lookAheadBufferstoFollow);
        container.add(optionalRolloverCounts);

        return container;

    }

    private AFSDFieldPackager getDeviceFaultPackager() {

        FSDMsgX container = new FSDMsgX("DeviceFault");

        AFSDFieldPackager fs6 = new FixedFieldPackager(Fields.FIELD_SEPARATOR_6,
                strFS, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager deviceStatuses = new VariableFieldPackager(
                Fields.DEVICE_STATUSES, fs, AsciiInterpreter.INSTANCE);

        AFSDFieldPackager useIfSet = new VariableFieldPackager(
                Fields.COINS_DISPENSED, 3, fs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager lookAheadCoinsDispensed = new LookAheadPackager(
                "LookForCoins", 4, fs, useIfSet, null,
                new String[] { Fields.COINS_DISPENSED }, null);
        AFSDFieldPackager useIfSet2 = new VariableFieldPackager(
                Fields.MDS_STATUS, fs, AsciiInterpreter.INSTANCE);

        AFSDFieldPackager lookAheadMDSStatus = new LookAheadPackager(
                "LookForMDSStatusColon", 5, new Byte((byte) ':'), useIfSet2,
                null, new String[] { Fields.MDS_STATUS }, null);

        AFSDFieldPackager useIfSet3 = new VariableFieldPackager(
                Fields.BUFFERS_FOLLOWING, fs, AsciiInterpreter.INSTANCE);

        AFSDFieldPackager lookAheadBufferstoFollow = new LookAheadPackager(
                "LookForBufferToFollow", 0, new Byte((byte) 0x39), useIfSet3,
                null, new String[] { Fields.BUFFERS_FOLLOWING }, null);

        AFSDFieldPackager optionalRolloverCounts = new OptionalPackager(
                "OptionalRolloverCounts", new FixedFieldPackager(
                        Fields.ROLLOVER_COUNTS, 12, AsciiInterpreter.INSTANCE));
        container.add(fs6);
        container.add(deviceStatuses);
        container.add(lookAheadCoinsDispensed);
        container.add(lookAheadMDSStatus);
        container.add(lookAheadBufferstoFollow);
        container.add(optionalRolloverCounts);

        new FixedFieldPackager(Fields.FIELD_SEPARATOR_6, strFS,
                AsciiInterpreter.INSTANCE);

        return container;

    }

    public AFSDFieldPackager getUnsolicitedPackager() {

        FSDMsgX creq = new FSDMsgX("CREQ");

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
                ifSetTimeVariantPreSep, null,
                new String[] { Fields.TIME_VARIANT }, null);
        AFSDFieldPackager fs3 = new FixedFieldPackager(Fields.FIELD_SEPARATOR_3,
                strFS, LiteralInterpreter.INSTANCE);
        AFSDFieldPackager topOfReceipt = new FixedFieldPackager(
                Fields.TOP_OF_RECEIPT, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager messageCoOrdNumber = new FixedFieldPackager(
                Fields.MsgCoOrdNumber, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager fs4 = new FixedFieldPackager(Fields.FIELD_SEPARATOR_4,
                strFS, LiteralInterpreter.INSTANCE);
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
        AFSDFieldPackager track1ID = new FixedFieldPackager(Fields.TRACK1_ID, 1,
                AsciiInterpreter.INSTANCE);
        AFSDFieldPackager track1 = new VariableFieldPackager(Fields.TRACK1, 78,
                fs, AsciiInterpreter.INSTANCE);
        FSDMsgX ifSetTrack1ID = new FSDMsgX("IFSetTrack1ID");
        ifSetTrack1ID.add(track1ID);
        ifSetTrack1ID.add(track1);

        AFSDFieldPackager lookAheadTrack1ID = new LookAheadPackager(
                "LookAheadTrack1ID", 0, new Byte((byte) 0x31), ifSetTrack1ID,
                null, new String[] { Fields.TRACK1 }, null);
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
                new String[] { Fields.MICR }, null);
                // END: Optional TransactionVerification status

        // Begin: if Optional TRAN Verification Data is present
        AFSDFieldPackager tranVeriID = new FixedFieldPackager(
                Fields.TRAN_VERIFICATION_ID, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager serialNumberInLastFuncCommand = new FixedFieldPackager(
                Fields.SERIAL_NUMBER, 4, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager responseToLastFuncCommand = new FixedFieldPackager(
                Fields.RESPONSE_TO_LAST_FUNCTION_CMD, 1,
                AsciiInterpreter.INSTANCE);
        AFSDFieldPackager retractOp = new FixedFieldPackager(
                Fields.RETRACT_OPERATION, 1, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager gs1 = new FixedFieldPackager("GS1", strGS,
                LiteralInterpreter.INSTANCE);
        AFSDFieldPackager deviceDispenser = new FixedFieldPackager(
                Fields.DEVICE_ID_DISPENSER, "DI01", AsciiInterpreter.INSTANCE);
        AFSDFieldPackager rs1 = new FixedFieldPackager("rs1", strRS,
                LiteralInterpreter.INSTANCE);

        AFSDFieldPackager denomIDs = new VariableFieldPackager(
                Fields.CASSETTES_IDS, 5, rs, AsciiInterpreter.INSTANCE);
        AFSDFieldPackager dispensedCounts = new VariableFieldPackager(
                Fields.DISPENSE_COUNTS, 25, fs, AsciiInterpreter.INSTANCE);

        // There can be a fifth cassette and the char after that can be a
        // GS : If coin dispenser is present
        // FS: If coin dispenser absent and Buffer ID and or Rollover counts

        // TODO handle data after Transaction verification status

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
                "LookAheadtranVeriID", 0, new Byte((byte) 0x36),
                ifSettranVeriID, null,
                new String[] { Fields.RESPONSE_TO_LAST_FUNCTION_CMD }, null);
        // End: if Optional TRAN Verification Data is present

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

    public static void main(String[] args) {// throws ISOException {
        ATMMessage m = new ATMMessage();

        FSDMsgX msg = m.getMessagePackager();
        try {
            msg.unpack(ISOUtil.hex2byte(
                    "31311C3535353131313132321C1C35353936334345371C31361C3B353838383833383630303030303530363537343D31353130313031313031303030303F1C1C41414220202041431C30303030303030301C343E36313A34383A37353B3A3637363D1C1C1C311C363030303131301D444930311E414243441E30303030303030303031303030303130303030301C3032313030313030343030301C4337383536454535")); // consumer req
            // "32321C3535353131313132321C1C391C303231303031303034303030")); //solicited status
        }
        catch (ISOException ex) {
            // Packager is incomplete  , it will throw an exception
            ex.printStackTrace();
        }
        finally {
            System.out.println(msg.getParserTree(""));
            System.out.println(msg.dump(""));

        }
    }

}
