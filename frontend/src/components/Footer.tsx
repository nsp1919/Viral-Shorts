import Link from "next/link";
import { Instagram } from "lucide-react";

export default function Footer() {
    return (
        <footer className="py-8 text-center text-sm bg-zinc-900 text-zinc-400 border-t border-zinc-800">
            <p>© 2025 AutoShorts AI. Built for creators.</p>
            <div className="mt-4 flex flex-col items-center gap-2">
                <p>
                    Developed by{" "}
                    <Link
                        href="https://nspcreativehub.info"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-purple-400 hover:text-purple-300 hover:underline font-medium transition-colors"
                    >
                        NSP Creative Hub
                    </Link>
                </p>
                <div className="flex items-center gap-4">
                    <Link
                        href="https://mail.google.com/mail/?view=cm&to=nspcreativehub@gmail.com"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-zinc-400 hover:text-purple-400 transition-colors"
                    >
                        nspcreativehub@gmail.com
                    </Link>
                    <span className="text-zinc-600">|</span>
                    <Link
                        href="https://www.instagram.com/nsp_creative_hub"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-1.5 text-zinc-400 hover:text-pink-400 transition-colors"
                    >
                        <Instagram className="h-4 w-4" />
                        Follow us on Instagram
                    </Link>
                </div>
            </div>
        </footer>
    );
}

