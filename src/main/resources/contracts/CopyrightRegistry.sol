// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract CopyrightRegistry {

    // 1. 定义一个结构体来存储版权记录
    struct CopyrightRecord {
        string softwareName;      // 软件名称
        address owner;            // 版权拥有者地址
        uint256 timestamp;        // 存证时间戳
        string description;       // 软件描述
        bool isRegistered;        // 是否已注册
    }

    // 2. 创建一个映射 (map)，以文件哈希值作为键，存储对应的版权记录
    mapping(bytes32 => CopyrightRecord) public records;

    // 3. 定义一个事件，当成功申请版权时触发，方便前端监听和获取交易信息
    event CopyrightApplied(
        bytes32 indexed fileHash, // 便于索引，加快查询
        string softwareName,
        address owner,
        uint256 timestamp
    );

    // 4. 核心函数：申请版权存证
    function applyForCopyright(
        bytes32 _fileHash,
        string memory _softwareName,
        string memory _description
    ) public {
        // 检查该哈希是否已被注册
        require(!records[_fileHash].isRegistered, "Copyright already exists for this file hash.");

        // 创建新的版权记录
        records[_fileHash] = CopyrightRecord({
            softwareName: _softwareName,
            owner: msg.sender, // 版权自动归属为调用此函数的地址
            timestamp: block.timestamp,
            description: _description,
            isRegistered: true
        });

        // 触发事件
        emit CopyrightApplied(_fileHash, _softwareName, msg.sender, block.timestamp);
    }

    // 5. 核心函数：根据文件哈希查询存证信息
    function queryByHash(bytes32 _fileHash) public view returns (
        string memory softwareName,
        address owner,
        uint256 timestamp,
        string memory description,
        bool isRegistered
    ) {
        CopyrightRecord memory record = records[_fileHash];
        return (
            record.softwareName,
            record.owner,
            record.timestamp,
            record.description,
            record.isRegistered
        );
    }
}