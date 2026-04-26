package cn.blockchain.copyrightsoft.contracts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bool;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple5;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.eventsub.EventSubCallback;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class SoftwareEvidenceAnchor extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50610988806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806301e6472514610051578063054372ed146100855780638f6459e5146100a1578063db16d31b146100d5575b600080fd5b61006b600480360381019061006691906105b0565b6100f1565b60405161007c95949392919061067d565b60405180910390f35b61009f600480360381019061009a91906106fc565b610161565b005b6100bb60048036038101906100b691906105b0565b6102cf565b6040516100cc95949392919061067d565b60405180910390f35b6100ef60048036038101906100ea919061073c565b6103c6565b005b60006020528060005260406000206000915090508060000154908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060020154908060030160009054906101000a900460ff16908060030160019054906101000a900460ff16905085565b600080600084815260200190815260200160002090508060030160019054906101000a900460ff166101c8576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101bf906107ec565b60405180910390fd5b3373ffffffffffffffffffffffffffffffffffffffff168160010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161461025a576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161025190610858565b60405180910390fd5b60008160030160009054906101000a900460ff169050828260030160006101000a81548160ff021916908360ff160217905550837faa11821677914b32786df643993064d9351dbfa3e85c40308abea9b662f36f9e8285426040516102c193929190610878565b60405180910390a250505050565b6000806000806000806000808881526020019081526020016000206040518060a0016040529081600082015481526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600282015481526020016003820160009054906101000a900460ff1660ff1660ff1681526020016003820160019054906101000a900460ff161515151581525050905080600001518160200151826040015183606001518460800151955095509550955095505091939590929450565b60008084815260200190815260200160002060030160019054906101000a900460ff1615610429576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610420906108fb565b60405180910390fd5b6040518060a001604052808381526020013373ffffffffffffffffffffffffffffffffffffffff1681526020014281526020018260ff168152602001600115158152506000808581526020019081526020016000206000820151816000015560208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506040820151816002015560608201518160030160006101000a81548160ff021916908360ff16021790555060808201518160030160016101000a81548160ff0219169083151502179055509050503373ffffffffffffffffffffffffffffffffffffffff16837f99c72eadbb8d4023e21cc111e343130c5a711da9016232bd38532da2bce1f7938442856040516105689392919061091b565b60405180910390a3505050565b600080fd5b6000819050919050565b61058d8161057a565b811461059857600080fd5b50565b6000813590506105aa81610584565b92915050565b6000602082840312156105c6576105c5610575565b5b60006105d48482850161059b565b91505092915050565b6105e68161057a565b82525050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610617826105ec565b9050919050565b6106278161060c565b82525050565b6000819050919050565b6106408161062d565b82525050565b600060ff82169050919050565b61065c81610646565b82525050565b60008115159050919050565b61067781610662565b82525050565b600060a08201905061069260008301886105dd565b61069f602083018761061e565b6106ac6040830186610637565b6106b96060830185610653565b6106c6608083018461066e565b9695505050505050565b6106d981610646565b81146106e457600080fd5b50565b6000813590506106f6816106d0565b92915050565b6000806040838503121561071357610712610575565b5b60006107218582860161059b565b9250506020610732858286016106e7565b9150509250929050565b60008060006060848603121561075557610754610575565b5b60006107638682870161059b565b93505060206107748682870161059b565b9250506040610785868287016106e7565b9150509250925092565b600082825260208201905092915050565b7f45766964656e6365206e6f7420666f756e642e00000000000000000000000000600082015250565b60006107d660138361078f565b91506107e1826107a0565b602082019050919050565b60006020820190508181036000830152610805816107c9565b9050919050565b7f4f6e6c79206f776e65722063616e20757064617465207374617475732e000000600082015250565b6000610842601d8361078f565b915061084d8261080c565b602082019050919050565b6000602082019050818103600083015261087181610835565b9050919050565b600060608201905061088d6000830186610653565b61089a6020830185610653565b6108a76040830184610637565b949350505050565b7f45766964656e636520616c7265616479206578697374732e0000000000000000600082015250565b60006108e560188361078f565b91506108f0826108af565b602082019050919050565b60006020820190508181036000830152610914816108d8565b9050919050565b600060608201905061093060008301866105dd565b61093d6020830185610637565b61094a6040830184610653565b94935050505056fea26469706673582212204ad33908c03c61ee62c0b6d6a122c22780cb08cd251355c5766478d954b036a464736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b50610988806100206000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c806355de76841461005157806376392b8e1461006d57806399ee34a9146100a1578063a506854b146100bd575b600080fd5b61006b600480360381019061006691906105e9565b6100f1565b005b6100876004803603810190610082919061063c565b6102a0565b6040516100989594939291906106fc565b60405180910390f35b6100bb60048036038101906100b6919061074f565b610310565b005b6100d760048036038101906100d2919061063c565b61047e565b6040516100e89594939291906106fc565b60405180910390f35b60008084815260200190815260200160002060030160019054906101000a900460ff1615610154576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040161014b906107ec565b60405180910390fd5b6040518060a001604052808381526020013373ffffffffffffffffffffffffffffffffffffffff1681526020014281526020018260ff168152602001600115158152506000808581526020019081526020016000206000820151816000015560208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506040820151816002015560608201518160030160006101000a81548160ff021916908360ff16021790555060808201518160030160016101000a81548160ff0219169083151502179055509050503373ffffffffffffffffffffffffffffffffffffffff16837fbf725904945c0cfea76d1da3d326f3d933edd1b02687dad94ede564ea11fa93d8442856040516102939392919061080c565b60405180910390a3505050565b60006020528060005260406000206000915090508060000154908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060020154908060030160009054906101000a900460ff16908060030160019054906101000a900460ff16905085565b600080600084815260200190815260200160002090508060030160019054906101000a900460ff16610377576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040161036e9061088f565b60405180910390fd5b3373ffffffffffffffffffffffffffffffffffffffff168160010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614610409576040517fc703cb12000000000000000000000000000000000000000000000000000000008152600401610400906108fb565b60405180910390fd5b60008160030160009054906101000a900460ff169050828260030160006101000a81548160ff021916908360ff160217905550837fd82c397186cd822a5e626a2f7caaee2b581cd7fd3a395b644b352850b54d08e48285426040516104709392919061091b565b60405180910390a250505050565b6000806000806000806000808881526020019081526020016000206040518060a0016040529081600082015481526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600282015481526020016003820160009054906101000a900460ff1660ff1660ff1681526020016003820160019054906101000a900460ff161515151581525050905080600001518160200151826040015183606001518460800151955095509550955095505091939590929450565b600080fd5b6000819050919050565b61058d8161057a565b811461059857600080fd5b50565b6000813590506105aa81610584565b92915050565b600060ff82169050919050565b6105c6816105b0565b81146105d157600080fd5b50565b6000813590506105e3816105bd565b92915050565b60008060006060848603121561060257610601610575565b5b60006106108682870161059b565b93505060206106218682870161059b565b9250506040610632868287016105d4565b9150509250925092565b60006020828403121561065257610651610575565b5b60006106608482850161059b565b91505092915050565b6106728161057a565b82525050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006106a382610678565b9050919050565b6106b381610698565b82525050565b6000819050919050565b6106cc816106b9565b82525050565b6106db816105b0565b82525050565b60008115159050919050565b6106f6816106e1565b82525050565b600060a0820190506107116000830188610669565b61071e60208301876106aa565b61072b60408301866106c3565b61073860608301856106d2565b61074560808301846106ed565b9695505050505050565b6000806040838503121561076657610765610575565b5b60006107748582860161059b565b9250506020610785858286016105d4565b9150509250929050565b600082825260208201905092915050565b7f45766964656e636520616c7265616479206578697374732e0000000000000000600082015250565b60006107d660188361078f565b91506107e1826107a0565b602082019050919050565b60006020820190508181036000830152610805816107c9565b9050919050565b60006060820190506108216000830186610669565b61082e60208301856106c3565b61083b60408301846106d2565b949350505050565b7f45766964656e6365206e6f7420666f756e642e00000000000000000000000000600082015250565b600061087960138361078f565b915061088482610843565b602082019050919050565b600060208201905081810360008301526108a88161086c565b9050919050565b7f4f6e6c79206f776e65722063616e20757064617465207374617475732e000000600082015250565b60006108e5601d8361078f565b91506108f0826108af565b602082019050919050565b60006020820190508181036000830152610914816108d8565b9050919050565b600060608201905061093060008301866106d2565b61093d60208301856106d2565b61094a60408301846106c3565b94935050505056fea26469706673582212204a0cdde794ff2701ef0ba8b791d26b1f13440f36d46fd20f883d91f9831f6ea364736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"bytes32\",\"name\":\"evidenceRootHash\",\"type\":\"bytes32\"},{\"indexed\":false,\"internalType\":\"bytes32\",\"name\":\"metadataHash\",\"type\":\"bytes32\"},{\"indexed\":true,\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"},{\"indexed\":false,\"internalType\":\"uint8\",\"name\":\"status\",\"type\":\"uint8\"}],\"name\":\"EvidenceRegistered\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"bytes32\",\"name\":\"evidenceRootHash\",\"type\":\"bytes32\"},{\"indexed\":false,\"internalType\":\"uint8\",\"name\":\"oldStatus\",\"type\":\"uint8\"},{\"indexed\":false,\"internalType\":\"uint8\",\"name\":\"newStatus\",\"type\":\"uint8\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"EvidenceStatusUpdated\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"evidenceRootHash\",\"type\":\"bytes32\"}],\"name\":\"queryEvidence\",\"outputs\":[{\"internalType\":\"bytes32\",\"name\":\"metadataHash\",\"type\":\"bytes32\"},{\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"},{\"internalType\":\"uint8\",\"name\":\"status\",\"type\":\"uint8\"},{\"internalType\":\"bool\",\"name\":\"isRegistered\",\"type\":\"bool\"}],\"selector\":[2405718501,2768667979],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"records\",\"outputs\":[{\"internalType\":\"bytes32\",\"name\":\"metadataHash\",\"type\":\"bytes32\"},{\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"},{\"internalType\":\"uint8\",\"name\":\"status\",\"type\":\"uint8\"},{\"internalType\":\"bool\",\"name\":\"isRegistered\",\"type\":\"bool\"}],\"selector\":[31868709,1983458190],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"evidenceRootHash\",\"type\":\"bytes32\"},{\"internalType\":\"bytes32\",\"name\":\"metadataHash\",\"type\":\"bytes32\"},{\"internalType\":\"uint8\",\"name\":\"status\",\"type\":\"uint8\"}],\"name\":\"registerEvidence\",\"outputs\":[],\"selector\":[3675706139,1440642692],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"evidenceRootHash\",\"type\":\"bytes32\"},{\"internalType\":\"uint8\",\"name\":\"newStatus\",\"type\":\"uint8\"}],\"name\":\"updateStatus\",\"outputs\":[],\"selector\":[88306413,2582525097],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_QUERYEVIDENCE = "queryEvidence";

    public static final String FUNC_RECORDS = "records";

    public static final String FUNC_REGISTEREVIDENCE = "registerEvidence";

    public static final String FUNC_UPDATESTATUS = "updateStatus";

    public static final Event EVIDENCEREGISTERED_EVENT = new Event("EvidenceRegistered", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}));
    ;

    public static final Event EVIDENCESTATUSUPDATED_EVENT = new Event("EvidenceStatusUpdated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}));
    ;

    protected SoftwareEvidenceAnchor(String contractAddress, Client client,
            CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<EvidenceRegisteredEventResponse> getEvidenceRegisteredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(EVIDENCEREGISTERED_EVENT, transactionReceipt);
        ArrayList<EvidenceRegisteredEventResponse> responses = new ArrayList<EvidenceRegisteredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EvidenceRegisteredEventResponse typedResponse = new EvidenceRegisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.evidenceRootHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.metadataHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.status = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeEvidenceRegisteredEvent(BigInteger fromBlock, BigInteger toBlock,
            List<String> otherTopics, EventSubCallback callback) {
        String topic0 = eventEncoder.encode(EVIDENCEREGISTERED_EVENT);
        subscribeEvent(topic0,otherTopics,fromBlock,toBlock,callback);
    }

    public void subscribeEvidenceRegisteredEvent(EventSubCallback callback) {
        String topic0 = eventEncoder.encode(EVIDENCEREGISTERED_EVENT);
        subscribeEvent(topic0,callback);
    }

    public List<EvidenceStatusUpdatedEventResponse> getEvidenceStatusUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(EVIDENCESTATUSUPDATED_EVENT, transactionReceipt);
        ArrayList<EvidenceStatusUpdatedEventResponse> responses = new ArrayList<EvidenceStatusUpdatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EvidenceStatusUpdatedEventResponse typedResponse = new EvidenceStatusUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.evidenceRootHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.oldStatus = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newStatus = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeEvidenceStatusUpdatedEvent(BigInteger fromBlock, BigInteger toBlock,
            List<String> otherTopics, EventSubCallback callback) {
        String topic0 = eventEncoder.encode(EVIDENCESTATUSUPDATED_EVENT);
        subscribeEvent(topic0,otherTopics,fromBlock,toBlock,callback);
    }

    public void subscribeEvidenceStatusUpdatedEvent(EventSubCallback callback) {
        String topic0 = eventEncoder.encode(EVIDENCESTATUSUPDATED_EVENT);
        subscribeEvent(topic0,callback);
    }

    public Tuple5<byte[], String, BigInteger, BigInteger, Boolean> queryEvidence(
            byte[] evidenceRootHash) throws ContractException {
        final Function function = new Function(FUNC_QUERYEVIDENCE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple5<byte[], String, BigInteger, BigInteger, Boolean>(

                (byte[]) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (BigInteger) results.get(2).getValue(), 
                (BigInteger) results.get(3).getValue(), 
                (Boolean) results.get(4).getValue()
                );
    }

    public Function getMethodQueryEvidenceRawFunction(byte[] evidenceRootHash) throws
            ContractException {
        final Function function = new Function(FUNC_QUERYEVIDENCE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        return function;
    }

    public Tuple5<byte[], String, BigInteger, BigInteger, Boolean> records(byte[] param0) throws
            ContractException {
        final Function function = new Function(FUNC_RECORDS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple5<byte[], String, BigInteger, BigInteger, Boolean>(

                (byte[]) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (BigInteger) results.get(2).getValue(), 
                (BigInteger) results.get(3).getValue(), 
                (Boolean) results.get(4).getValue()
                );
    }

    public Function getMethodRecordsRawFunction(byte[] param0) throws ContractException {
        final Function function = new Function(FUNC_RECORDS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        return function;
    }

    /**
     * @return TransactionReceipt Get more transaction info (e.g. txhash, block) from TransactionReceipt 
     */
    public TransactionReceipt registerEvidence(byte[] evidenceRootHash, byte[] metadataHash,
            BigInteger status) {
        final Function function = new Function(
                FUNC_REGISTEREVIDENCE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(metadataHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(status)), 
                Collections.<TypeReference<?>>emptyList(), 4);
        return executeTransaction(function);
    }

    public Function getMethodRegisterEvidenceRawFunction(byte[] evidenceRootHash,
            byte[] metadataHash, BigInteger status) throws ContractException {
        final Function function = new Function(FUNC_REGISTEREVIDENCE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(metadataHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(status)), 
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForRegisterEvidence(byte[] evidenceRootHash,
            byte[] metadataHash, BigInteger status) {
        final Function function = new Function(
                FUNC_REGISTEREVIDENCE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(metadataHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(status)), 
                Collections.<TypeReference<?>>emptyList(), 4);
        return createSignedTransaction(function);
    }

    /**
     * @param callback Get TransactionReceipt from TransactionCallback onResponse(TransactionReceipt receipt) 
     * @return txHash Transaction hash of current transaction call 
     */
    public String registerEvidence(byte[] evidenceRootHash, byte[] metadataHash, BigInteger status,
            TransactionCallback callback) {
        final Function function = new Function(
                FUNC_REGISTEREVIDENCE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(metadataHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(status)), 
                Collections.<TypeReference<?>>emptyList(), 4);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple3<byte[], byte[], BigInteger> getRegisterEvidenceInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_REGISTEREVIDENCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint8>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<byte[], byte[], BigInteger>(

                (byte[]) results.get(0).getValue(), 
                (byte[]) results.get(1).getValue(), 
                (BigInteger) results.get(2).getValue()
                );
    }

    /**
     * @return TransactionReceipt Get more transaction info (e.g. txhash, block) from TransactionReceipt 
     */
    public TransactionReceipt updateStatus(byte[] evidenceRootHash, BigInteger newStatus) {
        final Function function = new Function(
                FUNC_UPDATESTATUS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(newStatus)), 
                Collections.<TypeReference<?>>emptyList(), 4);
        return executeTransaction(function);
    }

    public Function getMethodUpdateStatusRawFunction(byte[] evidenceRootHash, BigInteger newStatus)
            throws ContractException {
        final Function function = new Function(FUNC_UPDATESTATUS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(newStatus)), 
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForUpdateStatus(byte[] evidenceRootHash,
            BigInteger newStatus) {
        final Function function = new Function(
                FUNC_UPDATESTATUS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(newStatus)), 
                Collections.<TypeReference<?>>emptyList(), 4);
        return createSignedTransaction(function);
    }

    /**
     * @param callback Get TransactionReceipt from TransactionCallback onResponse(TransactionReceipt receipt) 
     * @return txHash Transaction hash of current transaction call 
     */
    public String updateStatus(byte[] evidenceRootHash, BigInteger newStatus,
            TransactionCallback callback) {
        final Function function = new Function(
                FUNC_UPDATESTATUS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(evidenceRootHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint8(newStatus)), 
                Collections.<TypeReference<?>>emptyList(), 4);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<byte[], BigInteger> getUpdateStatusInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_UPDATESTATUS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint8>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<byte[], BigInteger>(

                (byte[]) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public static SoftwareEvidenceAnchor load(String contractAddress, Client client,
            CryptoKeyPair credential) {
        return new SoftwareEvidenceAnchor(contractAddress, client, credential);
    }

    public static SoftwareEvidenceAnchor deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(SoftwareEvidenceAnchor.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }

    public static class EvidenceRegisteredEventResponse {
        public TransactionReceipt.Logs log;

        public byte[] evidenceRootHash;

        public String owner;

        public byte[] metadataHash;

        public BigInteger timestamp;

        public BigInteger status;
    }

    public static class EvidenceStatusUpdatedEventResponse {
        public TransactionReceipt.Logs log;

        public byte[] evidenceRootHash;

        public BigInteger oldStatus;

        public BigInteger newStatus;

        public BigInteger timestamp;
    }
}
