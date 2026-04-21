package cn.blockchain.copyrightsoft;

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
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
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
public class CopyrightRegistry extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50610b86806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c806301e647251461004657806388f8ebd01461007a578063a2cb7c9514610096575b600080fd5b610060600480360381019061005b91906106c3565b6100ca565b6040516100719594939291906107fe565b60405180910390f35b610094600480360381019061008f9190610994565b61023d565b005b6100b060048036038101906100ab91906106c3565b6103e1565b6040516100c19594939291906107fe565b60405180910390f35b60006020528060005260406000206000915090508060000180546100ed90610a4e565b80601f016020809104026020016040519081016040528092919081815260200182805461011990610a4e565b80156101665780601f1061013b57610100808354040283529160200191610166565b820191906000526020600020905b81548152906001019060200180831161014957829003601f168201915b5050505050908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060020154908060030180546101a790610a4e565b80601f01602080910402602001604051908101604052809291908181526020018280546101d390610a4e565b80156102205780601f106101f557610100808354040283529160200191610220565b820191906000526020600020905b81548152906001019060200180831161020357829003601f168201915b5050505050908060040160009054906101000a900460ff16905085565b60008084815260200190815260200160002060040160009054906101000a900460ff16156102a0576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161029790610af2565b60405180910390fd5b6040518060a001604052808381526020013373ffffffffffffffffffffffffffffffffffffffff16815260200142815260200182815260200160011515815250600080858152602001908152602001600020600082015181600001908051906020019061030e9291906105d6565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020155606082015181600301908051906020019061037c9291906105d6565b5060808201518160040160006101000a81548160ff021916908315150217905550905050827f54070ae329cc7d35e334a1f963bb6002b9139323ddcced9b1cafd64087817b238333426040516103d493929190610b12565b60405180910390a2505050565b606060008060606000806000808881526020019081526020016000206040518060a001604052908160008201805461041890610a4e565b80601f016020809104026020016040519081016040528092919081815260200182805461044490610a4e565b80156104915780601f1061046657610100808354040283529160200191610491565b820191906000526020600020905b81548152906001019060200180831161047457829003601f168201915b505050505081526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016002820154815260200160038201805461050a90610a4e565b80601f016020809104026020016040519081016040528092919081815260200182805461053690610a4e565b80156105835780601f1061055857610100808354040283529160200191610583565b820191906000526020600020905b81548152906001019060200180831161056657829003601f168201915b505050505081526020016004820160009054906101000a900460ff161515151581525050905080600001518160200151826040015183606001518460800151955095509550955095505091939590929450565b8280546105e290610a4e565b90600052602060002090601f016020900481019282610604576000855561064b565b82601f1061061d57805160ff191683800117855561064b565b8280016001018555821561064b579182015b8281111561064a57825182559160200191906001019061062f565b5b509050610658919061065c565b5090565b5b8082111561067557600081600090555060010161065d565b5090565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b6106a08161068d565b81146106ab57600080fd5b50565b6000813590506106bd81610697565b92915050565b6000602082840312156106d9576106d8610683565b5b60006106e7848285016106ae565b91505092915050565b600081519050919050565b600082825260208201905092915050565b60005b8381101561072a57808201518184015260208101905061070f565b83811115610739576000848401525b50505050565b6000601f19601f8301169050919050565b600061075b826106f0565b61076581856106fb565b935061077581856020860161070c565b61077e8161073f565b840191505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006107b482610789565b9050919050565b6107c4816107a9565b82525050565b6000819050919050565b6107dd816107ca565b82525050565b60008115159050919050565b6107f8816107e3565b82525050565b600060a08201905081810360008301526108188188610750565b905061082760208301876107bb565b61083460408301866107d4565b81810360608301526108468185610750565b905061085560808301846107ef565b9695505050505050565b600080fd5b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6108a18261073f565b810181811067ffffffffffffffff821117156108c0576108bf610869565b5b80604052505050565b60006108d3610679565b90506108df8282610898565b919050565b600067ffffffffffffffff8211156108ff576108fe610869565b5b6109088261073f565b9050602081019050919050565b82818337600083830152505050565b6000610937610932846108e4565b6108c9565b90508281526020810184848401111561095357610952610864565b5b61095e848285610915565b509392505050565b600082601f83011261097b5761097a61085f565b5b813561098b848260208601610924565b91505092915050565b6000806000606084860312156109ad576109ac610683565b5b60006109bb868287016106ae565b935050602084013567ffffffffffffffff8111156109dc576109db610688565b5b6109e886828701610966565b925050604084013567ffffffffffffffff811115610a0957610a08610688565b5b610a1586828701610966565b9150509250925092565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b60006002820490506001821680610a6657607f821691505b60208210811415610a7a57610a79610a1f565b5b50919050565b7f436f7079726967687420616c72656164792065786973747320666f722074686960008201527f732066696c6520686173682e0000000000000000000000000000000000000000602082015250565b6000610adc602c836106fb565b9150610ae782610a80565b604082019050919050565b60006020820190508181036000830152610b0b81610acf565b9050919050565b60006060820190508181036000830152610b2c8186610750565b9050610b3b60208301856107bb565b610b4860408301846107d4565b94935050505056fea2646970667358221220a308124397681eda2d74edaec36e422a9c5522c31081bdfc5c2dfd5edc6bcd5364736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b50610b86806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c806361bee04d1461004657806376392b8e1461007a578063c9960364146100ae575b600080fd5b610060600480360381019061005b91906106c3565b6100ca565b6040516100719594939291906107fe565b60405180910390f35b610094600480360381019061008f91906106c3565b6102bf565b6040516100a59594939291906107fe565b60405180910390f35b6100c860048036038101906100c39190610994565b610432565b005b606060008060606000806000808881526020019081526020016000206040518060a001604052908160008201805461010190610a4e565b80601f016020809104026020016040519081016040528092919081815260200182805461012d90610a4e565b801561017a5780601f1061014f5761010080835404028352916020019161017a565b820191906000526020600020905b81548152906001019060200180831161015d57829003601f168201915b505050505081526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600282015481526020016003820180546101f390610a4e565b80601f016020809104026020016040519081016040528092919081815260200182805461021f90610a4e565b801561026c5780601f106102415761010080835404028352916020019161026c565b820191906000526020600020905b81548152906001019060200180831161024f57829003601f168201915b505050505081526020016004820160009054906101000a900460ff161515151581525050905080600001518160200151826040015183606001518460800151955095509550955095505091939590929450565b60006020528060005260406000206000915090508060000180546102e290610a4e565b80601f016020809104026020016040519081016040528092919081815260200182805461030e90610a4e565b801561035b5780601f106103305761010080835404028352916020019161035b565b820191906000526020600020905b81548152906001019060200180831161033e57829003601f168201915b5050505050908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169080600201549080600301805461039c90610a4e565b80601f01602080910402602001604051908101604052809291908181526020018280546103c890610a4e565b80156104155780601f106103ea57610100808354040283529160200191610415565b820191906000526020600020905b8154815290600101906020018083116103f857829003601f168201915b5050505050908060040160009054906101000a900460ff16905085565b60008084815260200190815260200160002060040160009054906101000a900460ff1615610495576040517fc703cb1200000000000000000000000000000000000000000000000000000000815260040161048c90610af2565b60405180910390fd5b6040518060a001604052808381526020013373ffffffffffffffffffffffffffffffffffffffff1681526020014281526020018281526020016001151581525060008085815260200190815260200160002060008201518160000190805190602001906105039291906105d6565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506040820151816002015560608201518160030190805190602001906105719291906105d6565b5060808201518160040160006101000a81548160ff021916908315150217905550905050827ff2241e7dd032668eabac184334a2f83b1f8af729462f71a139b39f0df7b9a75f8333426040516105c993929190610b12565b60405180910390a2505050565b8280546105e290610a4e565b90600052602060002090601f016020900481019282610604576000855561064b565b82601f1061061d57805160ff191683800117855561064b565b8280016001018555821561064b579182015b8281111561064a57825182559160200191906001019061062f565b5b509050610658919061065c565b5090565b5b8082111561067557600081600090555060010161065d565b5090565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b6106a08161068d565b81146106ab57600080fd5b50565b6000813590506106bd81610697565b92915050565b6000602082840312156106d9576106d8610683565b5b60006106e7848285016106ae565b91505092915050565b600081519050919050565b600082825260208201905092915050565b60005b8381101561072a57808201518184015260208101905061070f565b83811115610739576000848401525b50505050565b6000601f19601f8301169050919050565b600061075b826106f0565b61076581856106fb565b935061077581856020860161070c565b61077e8161073f565b840191505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006107b482610789565b9050919050565b6107c4816107a9565b82525050565b6000819050919050565b6107dd816107ca565b82525050565b60008115159050919050565b6107f8816107e3565b82525050565b600060a08201905081810360008301526108188188610750565b905061082760208301876107bb565b61083460408301866107d4565b81810360608301526108468185610750565b905061085560808301846107ef565b9695505050505050565b600080fd5b600080fd5b7fb95aa35500000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6108a18261073f565b810181811067ffffffffffffffff821117156108c0576108bf610869565b5b80604052505050565b60006108d3610679565b90506108df8282610898565b919050565b600067ffffffffffffffff8211156108ff576108fe610869565b5b6109088261073f565b9050602081019050919050565b82818337600083830152505050565b6000610937610932846108e4565b6108c9565b90508281526020810184848401111561095357610952610864565b5b61095e848285610915565b509392505050565b600082601f83011261097b5761097a61085f565b5b813561098b848260208601610924565b91505092915050565b6000806000606084860312156109ad576109ac610683565b5b60006109bb868287016106ae565b935050602084013567ffffffffffffffff8111156109dc576109db610688565b5b6109e886828701610966565b925050604084013567ffffffffffffffff811115610a0957610a08610688565b5b610a1586828701610966565b9150509250925092565b7fb95aa35500000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b60006002820490506001821680610a6657607f821691505b60208210811415610a7a57610a79610a1f565b5b50919050565b7f436f7079726967687420616c72656164792065786973747320666f722074686960008201527f732066696c6520686173682e0000000000000000000000000000000000000000602082015250565b6000610adc602c836106fb565b9150610ae782610a80565b604082019050919050565b60006020820190508181036000830152610b0b81610acf565b9050919050565b60006060820190508181036000830152610b2c8186610750565b9050610b3b60208301856107bb565b610b4860408301846107d4565b94935050505056fea2646970667358221220c1788b7b556f628e6ae64395c357eff70e9a068fcd9dde258aff72b6687a03c164736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"bytes32\",\"name\":\"fileHash\",\"type\":\"bytes32\"},{\"indexed\":false,\"internalType\":\"string\",\"name\":\"softwareName\",\"type\":\"string\"},{\"indexed\":false,\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CopyrightApplied\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"_fileHash\",\"type\":\"bytes32\"},{\"internalType\":\"string\",\"name\":\"_softwareName\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"_description\",\"type\":\"string\"}],\"name\":\"applyForCopyright\",\"outputs\":[],\"selector\":[2298014672,3382051684],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"_fileHash\",\"type\":\"bytes32\"}],\"name\":\"queryByHash\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"softwareName\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"},{\"internalType\":\"string\",\"name\":\"description\",\"type\":\"string\"},{\"internalType\":\"bool\",\"name\":\"isRegistered\",\"type\":\"bool\"}],\"selector\":[2731244693,1639899213],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":3,\"slot\":0,\"value\":[0]}],\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"records\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"softwareName\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"timestamp\",\"type\":\"uint256\"},{\"internalType\":\"string\",\"name\":\"description\",\"type\":\"string\"},{\"internalType\":\"bool\",\"name\":\"isRegistered\",\"type\":\"bool\"}],\"selector\":[31868709,1983458190],\"stateMutability\":\"view\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_APPLYFORCOPYRIGHT = "applyForCopyright";

    public static final String FUNC_QUERYBYHASH = "queryByHash";

    public static final String FUNC_RECORDS = "records";

    public static final Event COPYRIGHTAPPLIED_EVENT = new Event("CopyrightApplied", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    protected CopyrightRegistry(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<CopyrightAppliedEventResponse> getCopyrightAppliedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(COPYRIGHTAPPLIED_EVENT, transactionReceipt);
        ArrayList<CopyrightAppliedEventResponse> responses = new ArrayList<CopyrightAppliedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CopyrightAppliedEventResponse typedResponse = new CopyrightAppliedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.fileHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.softwareName = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeCopyrightAppliedEvent(BigInteger fromBlock, BigInteger toBlock,
            List<String> otherTopics, EventSubCallback callback) {
        String topic0 = eventEncoder.encode(COPYRIGHTAPPLIED_EVENT);
        subscribeEvent(topic0,otherTopics,fromBlock,toBlock,callback);
    }

    public void subscribeCopyrightAppliedEvent(EventSubCallback callback) {
        String topic0 = eventEncoder.encode(COPYRIGHTAPPLIED_EVENT);
        subscribeEvent(topic0,callback);
    }

    /**
     * @return TransactionReceipt Get more transaction info (e.g. txhash, block) from TransactionReceipt 
     */
    public TransactionReceipt applyForCopyright(byte[] _fileHash, String _softwareName,
            String _description) {
        final Function function = new Function(
                FUNC_APPLYFORCOPYRIGHT, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(_fileHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_softwareName), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_description)), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public Function getMethodApplyForCopyrightRawFunction(byte[] _fileHash, String _softwareName,
            String _description) throws ContractException {
        final Function function = new Function(FUNC_APPLYFORCOPYRIGHT, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(_fileHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_softwareName), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_description)), 
                Arrays.<TypeReference<?>>asList());
        return function;
    }

    public String getSignedTransactionForApplyForCopyright(byte[] _fileHash, String _softwareName,
            String _description) {
        final Function function = new Function(
                FUNC_APPLYFORCOPYRIGHT, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(_fileHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_softwareName), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_description)), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    /**
     * @param callback Get TransactionReceipt from TransactionCallback onResponse(TransactionReceipt receipt) 
     * @return txHash Transaction hash of current transaction call 
     */
    public String applyForCopyright(byte[] _fileHash, String _softwareName, String _description,
            TransactionCallback callback) {
        final Function function = new Function(
                FUNC_APPLYFORCOPYRIGHT, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(_fileHash), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_softwareName), 
                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(_description)), 
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple3<byte[], String, String> getApplyForCopyrightInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_APPLYFORCOPYRIGHT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<byte[], String, String>(

                (byte[]) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (String) results.get(2).getValue()
                );
    }

    public Tuple5<String, String, BigInteger, String, Boolean> queryByHash(byte[] _fileHash) throws
            ContractException {
        final Function function = new Function(FUNC_QUERYBYHASH, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(_fileHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple5<String, String, BigInteger, String, Boolean>(

                (String) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (BigInteger) results.get(2).getValue(), 
                (String) results.get(3).getValue(), 
                (Boolean) results.get(4).getValue()
                );
    }

    public Function getMethodQueryByHashRawFunction(byte[] _fileHash) throws ContractException {
        final Function function = new Function(FUNC_QUERYBYHASH, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(_fileHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Bool>() {}));
        return function;
    }

    public Tuple5<String, String, BigInteger, String, Boolean> records(byte[] param0) throws
            ContractException {
        final Function function = new Function(FUNC_RECORDS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple5<String, String, BigInteger, String, Boolean>(

                (String) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (BigInteger) results.get(2).getValue(), 
                (String) results.get(3).getValue(), 
                (Boolean) results.get(4).getValue()
                );
    }

    public Function getMethodRecordsRawFunction(byte[] param0) throws ContractException {
        final Function function = new Function(FUNC_RECORDS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Bool>() {}));
        return function;
    }

    public static CopyrightRegistry load(String contractAddress, Client client,
            CryptoKeyPair credential) {
        return new CopyrightRegistry(contractAddress, client, credential);
    }

    public static CopyrightRegistry deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(CopyrightRegistry.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }

    public static class CopyrightAppliedEventResponse {
        public TransactionReceipt.Logs log;

        public byte[] fileHash;

        public String softwareName;

        public String owner;

        public BigInteger timestamp;
    }
}