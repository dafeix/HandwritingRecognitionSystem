package ge.edu.tsu.hrs.control_panel.console.cmd;

import ge.edu.tsu.hrs.control_panel.model.common.HRSPath;
import ge.edu.tsu.hrs.control_panel.model.network.NetworkProcessorType;
import ge.edu.tsu.hrs.control_panel.model.network.NetworkResult;
import ge.edu.tsu.hrs.control_panel.server.processor.common.HRSPathProcessor;
import ge.edu.tsu.hrs.control_panel.service.neuralnetwork.NeuralNetworkService;
import ge.edu.tsu.hrs.control_panel.service.neuralnetwork.NeuralNetworkServiceImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class NetworkResultConsoleApp {

    private static final HRSPathProcessor hrsPathProcessor = new HRSPathProcessor();

    private static final String cutSymbolsRootDirectory = hrsPathProcessor.getPath(HRSPath.CUT_SYMBOLS_PATH);

    private static final NeuralNetworkService neuralNetworkService = new NeuralNetworkServiceImpl(NetworkProcessorType.HRS_NEURAL_NETWORK);

    public static void main(String[] args) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println();
            System.out.println("ჩაირთო სიმბოლოს ამოცნობის აპლიკაცია!");
            System.out.println("ნებისმიერ მომენტში შეიყვანეთ retry აპლიკაციის თავიდან გასაშვებად");
            System.out.println();

            System.out.println("სიმბოლოს მისამართი: root მისამართიდან - " + cutSymbolsRootDirectory);
            String s = scanner.nextLine();
            if (isRetry(s)) {
                continue;
            }
            String symbolPath = cutSymbolsRootDirectory + s;
            System.out.println("სიმბოლოს სრული მისამართია - " + symbolPath);
            System.out.println();

            System.out.println("ქსელის id:");
            s = scanner.nextLine();
            if (isRetry(s)) {
                continue;
            }
            int networkId;
            try {
                networkId = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                continue;
            }

            System.out.println("ქსელის დამატებითი id:");
            s = scanner.nextLine();
            if (isRetry(s)) {
                continue;
            }
            int networkExtraId;
            try {
                networkExtraId = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                continue;
            }

            System.out.println("პარამეტრების შევსება დასრულდა. გსურთ დაპროცესირება? (true/false)");
            s = scanner.nextLine();
            if (isRetry(s)) {
                continue;
            }
            if (!Boolean.parseBoolean(s)) {
                continue;
            }
            try {
                BufferedImage image = ImageIO.read(new File(symbolPath));
                NetworkResult networkResult = neuralNetworkService.getNetworkResult(image, networkId, networkExtraId);
                System.out.println("ქსელის პასუხია - " + networkResult.getAnswer());
                System.out.println();

                System.out.println("გსურთ დეტალური ინფორმაციის ნახვა? (true/false)");
                s = scanner.nextLine();
                if (isRetry(s)) {
                    continue;
                }
                boolean info = Boolean.parseBoolean(s);
                if (info) {
                    for (int i = 0; i < networkResult.getOutputActivation().size(); i++) {
                        System.out.print(networkResult.getCharSequence().getIndexToCharMap().get(i) + " - " + networkResult.getOutputActivation().get(i) + "    ");
                        if ((i + 1) % 6 == 0) {
                            System.out.println();
                        }
                    }
                    System.out.println();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }

            System.out.println("გსურთ თავიდან გაშვება? (true/false)");
            s = scanner.nextLine();
            if (isRetry(s)) {
                continue;
            }
            boolean again = Boolean.parseBoolean(s);
            if (!again) {
                break;
            }
        }
    }

    private static boolean isRetry(String text) {
        return text.equals("retry");
    }
}
