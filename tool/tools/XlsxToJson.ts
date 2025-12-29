import * as XLSX from 'xlsx';
import * as fs from 'fs';
import * as path from 'path';
import { Project, StructureKind } from 'ts-morph';

//process.cwd()
const inputFolderPath: string = path.join(__dirname, '../xls/'); // 包含 .xlsx 文件的文件夹路径
// const outputFolderPath: string = path.join(__dirname, '../../client/assets/bundles/configs/'); // 输出 JSON 文件的文件夹路径
const outputClientFolderPath: string = path.join(__dirname, '../../assets/res/configs/'); // 输出 JSON 文件的文件夹路径
// const libFolderPath: string = path.join(__dirname, '../../game-service/config/'); // 输出 JSON 文件的文件夹路径
const libClientFolderPath: string = path.join(__dirname, '../../libs/'); // 输出 JSON 文件的文件夹路径
// 清理目录中的文件
// const cleanExportDirectory = () => {
//     let files = fs.readdirSync(outputFolderPath);
//     files.forEach(file => {
//         const filePath = path.join(outputFolderPath, file);
//         fs.unlinkSync(filePath);
//         console.log(`File "${filePath}" has been deleted.`);
//     });
// };

// 调用清理函数
// cleanExportDirectory();

// 获取目标文件夹中的所有文件
fs.readdir(inputFolderPath, (err, files) => {
    if (err) {
        console.error('Error reading directory:', err);
        return;
    }

    // 获取目标文件夹中的所有文件，排除以 '~$' 开头的文件
    const filteredFiles = files.filter(file => !file.startsWith('~$') && file.endsWith('.xlsx'));

    try {
        // fs.unlinkSync(libFolderPath + 'config.d.ts');
        fs.unlinkSync(libClientFolderPath + 'config.d.ts');
    } catch (error) {

    }
    const project = new Project();
    // const tsFile = project.createSourceFile(libFolderPath + 'config.d.ts');

    const clientTsFile = project.createSourceFile(libClientFolderPath + 'config.d.ts');
    // 对每个文件进行处理
    filteredFiles.forEach(file => {
        if (file.endsWith('.xlsx')) {
            const excelFilePath = `${inputFolderPath}${file}`;
            const workbook = XLSX.readFile(excelFilePath);
            const jsonSheets: { [sheetName: string]: any[] } = {};
            const ts: { [sheetName: string]: any[] } = {};

            workbook.SheetNames.forEach(sheetName => {
                const properties: Array<{ name: string, type: string, docs: Array<string> }> = [];
                const worksheet = workbook.Sheets[sheetName];
                const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 }) as Array<any>;

                const variableNamesRow = jsonData[0]; // 第一行为变量名
                const variableTypesRow = jsonData[2]; // 第二行为变量类型
                const commentsRow = jsonData[1]; // 第三行为注释
                if (variableNamesRow == undefined) {
                    return;
                }
                const sheetData = [];
                for (let i = 0; i < variableNamesRow.length; i++) {
                    let o: any = {};
                    const variableName = variableNamesRow[i];
                    const variableType = variableTypesRow[i];
                    o.docs = [commentsRow[i]];
                    switch (variableType) {
                        case 'int':
                        case 'float': {
                            o.type = 'number';
                            o.name = variableName;
                            break;
                        }
                        case 'array': {
                            o.type = 'Array<any>';
                            o.name = variableName;
                            break;
                        }
                        case 'array-float':
                        case 'array-int': {
                            o.type = 'Array<number>';
                            o.name = variableName;
                            break;
                        }
                        case 'date': {
                            o.type = 'Date'
                            o.name = variableName;
                            break;
                        }
                        case 'boolean': {
                            o.type = variableType;
                            o.name = variableName
                            break;
                        }
                        case 'multiple-float':
                        case 'multiple-int': {
                            o.type = 'Array<number>';
                            let a = variableName.split("_");
                            o.name = a[0];
                            break;
                        }

                        default: {
                            o.type = 'string';
                            o.name = variableName;
                            break;
                        }
                    }
                    let p = properties.find(p => p.name == o.name);
                    if (p) {
                        continue;
                    }
                    properties.push(o);
                }
                for (let i = 3; i < jsonData.length; i++) {
                    const row = jsonData[i];
                    const rowData: any = {};


                    for (let j = 0; j < row.length; j++) {
                        const variableName = variableNamesRow[j];
                        const variableType = variableTypesRow[j];
                        const cellValue = row[j];

                        // 根据类型进行解析
                        if (variableType === 'int') {
                            rowData[variableName] = parseInt(cellValue);
                        } else if (variableType === 'float') {
                            rowData[variableName] = parseFloat(cellValue);
                        } else if (variableType === 'array') {
                            // 假设数组以逗号分隔
                            rowData[variableName] = cellValue.split(',');
                        } else if (variableType === 'date') {
                            // 假设日期是以特定格式存储的，比如 'YYYY-MM-DD'
                            rowData[variableName] = new Date(cellValue);
                        } else if (variableType === 'boolean') {
                            // 解析布尔值，可以根据需要进行更复杂的逻辑
                            rowData[variableName] = cellValue === 'true' || cellValue == 'TRUE' || cellValue == true;
                        } else if (variableType === 'object') {
                            // 假设需要解析嵌套对象，例如 'key1:value1,key2:value2'
                            const objectData: any = {};
                            const keyValuePairs = cellValue.split(',');
                            keyValuePairs.forEach((pair: any) => {
                                const [key, value] = pair.split(':');
                                objectData[key.trim()] = value.trim();
                            });
                            rowData[variableName] = objectData;
                        } else if (variableType === 'multiple-int') {
                            let a = variableName.split("_");
                            let name = a[0] + "s";
                            rowData[name] = rowData[name] || [];
                            rowData[name][a[1]] = parseInt(cellValue);
                        } else if (variableType === 'multiple-float') {
                            let a = variableName.split("_");
                            let name = a[0] + "s";
                            rowData[name] = rowData[name] || [];
                            rowData[name][a[1]] = parseFloat(cellValue);
                        } else if (variableType === 'multiple-string') {
                            let a = variableName.split("_");
                            let name = a[0] + "s";
                            rowData[name] = rowData[name] || [];
                            rowData[name][a[1]] = cellValue + '';
                        } else if (variableType == 'array-int') {
                            let cs = (cellValue + "").split(',');
                            let ns: Array<number> = [];
                            cs.forEach((v: any) => {
                                ns.push(parseInt(v));
                            });
                            // 假设数组以逗号分隔
                            rowData[variableName] = ns;
                        } else if (variableType == 'array-float') {
                            let cs = (cellValue + '').split(',');
                            let ns: Array<number> = [];
                            cs.forEach((v: any) => {
                                ns.push(parseFloat(v));
                            });
                            // 假设数组以逗号分隔
                            rowData[variableName] = ns;
                        }else if (variableType == 'array2-string') {
                            let a1 = (cellValue + '').split(';');
                            let ns: Array<Array<string>> = [];
                            a1.forEach((v) => {
                                let cs = (v + '').split(",");
                                let s: Array<string> = [];
                                ns.push(s);
                                cs.forEach((v2) => {
                                    s.push(v2);
                                })
                            });
                            rowData[variableName] = ns;
                        }
                        else {
                            // 默认为字符串类型
                            rowData[variableName] = cellValue;
                        }
                    }

                    sheetData.push(rowData);
                }

                jsonSheets[sheetName] = sheetData;
                ts[sheetName] = properties;
            });

            for (const sheetName in jsonSheets) {
                const jsonFileName = `${sheetName}.json`;
                // const jsonFilePath = `${outputFolderPath}${jsonFileName}`;
                // fs.writeFileSync(jsonFilePath, JSON.stringify(jsonSheets[sheetName], null, 2));
                const jsonFilePath2 = `${outputClientFolderPath}${jsonFileName}`;
                fs.writeFileSync(jsonFilePath2, JSON.stringify(jsonSheets[sheetName], null, 2));
                // console.log(`JSON file "${jsonFilePath}" has been exported.`);
                // tsFile.addInterface({
                //     name: sheetName,
                //     isExported: true,
                //     properties: ts[sheetName]
                // })

                clientTsFile.addInterface({
                    name: sheetName,
                    isExported: false,
                    properties: ts[sheetName]
                })
            }
        }
    });

    project.saveSync();
});
